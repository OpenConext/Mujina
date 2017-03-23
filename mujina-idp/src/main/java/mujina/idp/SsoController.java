package mujina.idp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
public class SsoController {

  @Autowired
  private SAMLMessageHandler samlMessageHandler;

  //@PostMapping("/SingleSignOnService")
  @GetMapping("/SingleSignOnService")
  public void singleSignOnService(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
    response.sendRedirect("https://www.google.nl");
  }

}
