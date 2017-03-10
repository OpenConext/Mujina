/*
 * Copyright 2012 SURFnet bv, The Netherlands
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nl.surfnet.mujina.saml;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import nl.surfnet.mujina.model.CommonConfiguration;
import nl.surfnet.mujina.saml.xml.EndpointGenerator;
import nl.surfnet.mujina.saml.xml.LogoutResponseGenerator;
import nl.surfnet.mujina.util.IDService;
import nl.surfnet.mujina.util.TimeService;
import nl.surfnet.spring.security.opensaml.SAMLMessageHandler;
import org.apache.commons.lang.Validate;
import org.opensaml.saml2.core.LogoutResponse;
import org.opensaml.saml2.metadata.Endpoint;
import org.opensaml.ws.message.encoder.MessageEncodingException;
import org.opensaml.xml.security.CriteriaSet;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.security.credential.CredentialResolver;
import org.opensaml.xml.security.credential.UsageType;
import org.opensaml.xml.security.criteria.EntityIDCriteria;
import org.opensaml.xml.security.criteria.UsageCriteria;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.HttpRequestHandler;

public class SSOSuccessLogoutResponder implements HttpRequestHandler {

  private final TimeService timeService;
  private final IDService idService;
  private final SAMLMessageHandler adapter;
  private CredentialResolver credentialResolver;

  private final CommonConfiguration configuration;

  private static final Logger logger = LoggerFactory.getLogger(SSOSuccessLogoutResponder.class);

  public SSOSuccessLogoutResponder(TimeService timeService, IDService idService, SAMLMessageHandler adapter,
                                   CredentialResolver credentialResolver, CommonConfiguration configuration) {
    super();
    this.timeService = timeService;
    this.idService = idService;
    this.adapter = adapter;
    this.credentialResolver = credentialResolver;
    this.configuration = configuration;
  }

  @Override
  public void handleRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {

    CriteriaSet criteriaSet = new CriteriaSet();
    criteriaSet.add(new EntityIDCriteria(configuration.getEntityID()));
    criteriaSet.add(new UsageCriteria(UsageType.SIGNING));
    Credential signingCredential = getCredential(response, criteriaSet);

    if (signingCredential == null)
      return;

    LogoutResponseGenerator logoutResponseGenerator = new LogoutResponseGenerator(configuration.getEntityID(), timeService, idService);
    EndpointGenerator endpointGenerator = new EndpointGenerator();

    LogoutResponse logoutResponse = logoutResponseGenerator.buildLogoutResponse();

    String relayState = request.getParameter("RelayState");

    if (configuration.getSLOEndpoint() == null) {
      return;
    }

    String sloEndpointURL = configuration.getSLOEndpoint().getUrl();

    Endpoint endpoint = endpointGenerator.generateEndpoint(org.opensaml.saml2.metadata.SingleSignOnService.DEFAULT_ELEMENT_NAME,
      sloEndpointURL, null);

    try {
      adapter.sendSAMLMessage(logoutResponse, endpoint, response, relayState, signingCredential);
    } catch (MessageEncodingException mee) {
      logger.error("Exception encoding SAML message", mee);
      response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
    }
  }

  private Credential getCredential(HttpServletResponse response, CriteriaSet criteriaSet) throws IOException {
    Credential signingCredential;
    try {
      signingCredential = credentialResolver.resolveSingle(criteriaSet);
    } catch (org.opensaml.xml.security.SecurityException e) {
      logger.warn("Unable to resolve EntityID while signing", e);
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      return null;
    }
    Validate.notNull(signingCredential);
    return signingCredential;
  }
}
