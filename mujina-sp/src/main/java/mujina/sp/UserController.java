package mujina.sp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.saml.metadata.MetadataManager;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.Set;

@Controller
public class UserController {

  private static final Logger LOG = LoggerFactory.getLogger(UserController.class);

  @Autowired
  private MetadataManager metadata;


  @GetMapping("/discovery")
  @ResponseBody
  public Object idpSelection(HttpServletRequest request, Model model) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null)
      LOG.debug("Current authentication instance from security context is null");
    else
      LOG.debug("Current authentication instance from security context: {}", this.getClass().getSimpleName());
    if (auth == null || (auth instanceof AnonymousAuthenticationToken)) {
      Set<String> idps = metadata.getIDPEntityNames();
      for (String idp : idps)
        LOG.info("Configured Identity Provider for SSO: {}", idp);
      model.addAttribute("idps", idps);
      return idps;
    } else {
      LOG.warn("The current user is already logged.");
      return "[]";
    }
  }

  @GetMapping("/")
  public String index(Authentication authentication) {
    return authentication == null ? "index" : "redirect:/user.html";
  }

  @GetMapping({"user", "/user.html"})
  public String user(Authentication authentication, ModelMap modelMap) {
    modelMap.addAttribute("user", authentication.getPrincipal());
    return "user";
  }

}
