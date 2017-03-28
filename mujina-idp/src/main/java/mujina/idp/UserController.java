package mujina.idp;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

@Controller
public class UserController {

  @GetMapping("/")
  public String index(Authentication authentication) {
    return authentication == null || authentication instanceof AnonymousAuthenticationToken ? "index" : "redirect:/user.html";
  }

  @GetMapping("/user.html")
  public String user(Authentication authentication, ModelMap modelMap) {
    modelMap.addAttribute("user", authentication);
    return "user";
  }

}
