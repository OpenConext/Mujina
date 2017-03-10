package nl.surfnet.mujina.saml;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import nl.surfnet.mujina.spring.AuthnRequestInfo;
import org.opensaml.saml2.core.AuthnRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.web.WebAttributes;

public class SAMLAuthnRequestHandler implements SAMLRequestHandler<AuthnRequest> {
  private static final Logger logger = LoggerFactory.getLogger(SAMLAuthnRequestHandler.class);

  private final String authnResponderURI;

  public SAMLAuthnRequestHandler(String authnResponderURI) {
    this.authnResponderURI = authnResponderURI;
  }

  @Override public void handleSAMLRequest(HttpServletRequest request, HttpServletResponse response, AuthnRequest authnRequest)
    throws ServletException, IOException {

    AuthnRequestInfo info = new AuthnRequestInfo(authnRequest.getAssertionConsumerServiceURL(), authnRequest.getID(),
      authnRequest.getIssuer().getValue());

    logger.debug("AuthnRequest {} verified.  Forwarding to SSOSuccessAuthnResponder", info);
    request.getSession().setAttribute(AuthnRequestInfo.class.getName(), info);

    logger.debug("request.getSession().getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION) is {}",
      request.getSession().getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION));

    logger.debug("forwarding to authnResponderURI: {}", authnResponderURI);

    request.getRequestDispatcher(authnResponderURI).forward(request, response);
  }

  @Override public String getTypeLocalName() {
    return AuthnRequest.DEFAULT_ELEMENT_LOCAL_NAME;
  }
}
