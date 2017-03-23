package mujina.idp;

import mujina.saml.SAMLPrincipal;
import org.opensaml.ws.message.encoder.MessageEncodingException;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.signature.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class AuthenticationSuccessHandler implements org.springframework.security.web.authentication.AuthenticationSuccessHandler {

  private static final Logger LOG = LoggerFactory.getLogger(AuthenticationSuccessHandler.class);

  private final SAMLMessageHandler samlMessageHandler;

  public AuthenticationSuccessHandler(SAMLMessageHandler samlMessageHandler) {
    this.samlMessageHandler = samlMessageHandler;
  }

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
    LOG.debug("Sending response for successful authentication {}", authentication);

    SAMLPrincipal principal = (SAMLPrincipal) authentication.getPrincipal();
    try {
      samlMessageHandler.sendAuthnResponse(principal, response);
    } catch (MarshallingException | SignatureException | MessageEncodingException e) {
      throw new RuntimeException(e);
    }

  }

}
