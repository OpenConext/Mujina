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

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nl.surfnet.mujina.saml.xml.SAML2ValidatorSuite;
import nl.surfnet.mujina.spring.AuthnRequestInfo;

import org.opensaml.common.binding.SAMLMessageContext;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.ws.message.decoder.MessageDecodingException;
import org.opensaml.xml.security.SecurityException;
import org.opensaml.xml.validation.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.web.WebAttributes;
import org.springframework.web.HttpRequestHandler;

public class SingleSignOnService implements HttpRequestHandler {

  private final static Logger logger = LoggerFactory.getLogger(SingleSignOnService.class);

  private final BindingAdapter adapter;
  private final String authnResponderURI;
  private final SAML2ValidatorSuite validatorSuite;

  public SingleSignOnService(BindingAdapter adapter, String authnResponderURI, SAML2ValidatorSuite validatorSuite) {
    super();
    this.adapter = adapter;
    this.authnResponderURI = authnResponderURI;
    this.validatorSuite = validatorSuite;
  }

  @SuppressWarnings("rawtypes")
  @Override
  public void handleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    SAMLMessageContext messageContext = null;
    try {
      messageContext = adapter.extractSAMLMessageContext(request);
    } catch (MessageDecodingException mde) {
      logger.error("Exception decoding SAML message", mde);
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      return;
    } catch (SecurityException se) {
      logger.error("Exception decoding SAML message", se);
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      return;
    }

    AuthnRequest authnRequest = (AuthnRequest) messageContext.getInboundSAMLMessage();

    try {
      validatorSuite.validate(authnRequest);
    } catch (ValidationException ve) {
      logger.warn("AuthnRequest Message failed Validation", ve);
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      return;
    }

    AuthnRequestInfo info = new AuthnRequestInfo(authnRequest.getAssertionConsumerServiceURL(), authnRequest.getID());

    logger.debug("AuthnRequest {} vefified.  Forwarding to SSOSuccessAuthnResponder", info);
    request.getSession().setAttribute(AuthnRequestInfo.class.getName(), info);

    logger.debug("request.getSession().getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION) is {}",
        request.getSession().getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION));

    logger.debug("forwarding to authnResponderURI: {}", authnResponderURI);

    request.getRequestDispatcher(authnResponderURI).forward(request, response);

  }
}
