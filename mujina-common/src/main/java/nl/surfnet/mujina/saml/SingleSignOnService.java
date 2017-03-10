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
import java.util.List;

import nl.surfnet.mujina.saml.xml.SAML2ValidatorSuite;
import nl.surfnet.spring.security.opensaml.SAMLMessageHandler;
import org.opensaml.common.binding.SAMLMessageContext;
import org.opensaml.saml2.core.RequestAbstractType;
import org.opensaml.ws.message.decoder.MessageDecodingException;
import org.opensaml.xml.security.SecurityException;
import org.opensaml.xml.validation.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.HttpRequestHandler;

public class SingleSignOnService implements HttpRequestHandler {

  private static final Logger logger = LoggerFactory.getLogger(SingleSignOnService.class);

  private final SAMLMessageHandler adapter;
  private final SAML2ValidatorSuite validatorSuite;
  private final List<SAMLRequestHandler<RequestAbstractType>> requestHandlerList;

  public SingleSignOnService(SAMLMessageHandler adapter, SAML2ValidatorSuite validatorSuite,
                             List<SAMLRequestHandler<RequestAbstractType>> requestHandlerList) {
    super();
    this.adapter = adapter;
    this.validatorSuite = validatorSuite;
    this.requestHandlerList = requestHandlerList;
  }

  @SuppressWarnings("rawtypes")
  @Override
  public void handleRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
    SAMLMessageContext messageContext = null;
    try {
      messageContext = adapter.extractSAMLMessageContext(request);
    } catch (MessageDecodingException | SecurityException mde) {
      logger.error("Exception decoding SAML message", mde);
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      return;
    }

    final RequestAbstractType inboundSAMLMessage = (RequestAbstractType) messageContext.getInboundSAMLMessage();

    try {
      validatorSuite.validate(inboundSAMLMessage);
    } catch (ValidationException ve) {
      logger.warn("SAML Message failed Validation", ve);
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      return;
    }

    for (SAMLRequestHandler<RequestAbstractType> samlRequestHandler : requestHandlerList) {
      if (samlRequestHandler.getTypeLocalName().equals(inboundSAMLMessage.getElementQName().getLocalPart())) {
        samlRequestHandler.handleSAMLRequest(request, response, inboundSAMLMessage);
      }
    }

  }
}
