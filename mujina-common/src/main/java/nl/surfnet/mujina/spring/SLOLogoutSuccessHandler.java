package nl.surfnet.mujina.spring;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import nl.surfnet.mujina.saml.SLORequestSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AbstractAuthenticationTargetUrlRequestHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

public class SLOLogoutSuccessHandler extends AbstractAuthenticationTargetUrlRequestHandler implements LogoutSuccessHandler {
  private final SLORequestSender sloRequestSender;

  @Autowired public SLOLogoutSuccessHandler(SLORequestSender sloRequestSender) {
    this.sloRequestSender = sloRequestSender;
  }

  @Override public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
    throws IOException, ServletException {
    if (authentication != null) {
      sloRequestSender.sendSLORequest(authentication.getPrincipal().toString(), "");
    }

    super.handle(request, response, authentication);
  }
}
