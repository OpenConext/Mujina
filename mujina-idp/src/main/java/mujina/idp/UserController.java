package mujina.idp;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Controller
public class UserController {
  private List<Map<String, String>> samlAttributes;

  @Autowired
  @SuppressWarnings("unchecked")
  public UserController(ObjectMapper objectMapper) throws IOException {
    this.samlAttributes = objectMapper.readValue(new ClassPathResource("saml-attributes.json").getInputStream(), List.class);
  }

  @GetMapping("/")
  public String index(Authentication authentication) {
    return authentication == null ? "index" : "redirect:/user.html";
  }

  @GetMapping("/user.html")
  public String user(Authentication authentication, ModelMap modelMap) {
    modelMap.addAttribute("user", authentication);
    return "user";
  }

  @GetMapping("/login")
  public String login(ModelMap modelMap) {
    modelMap.addAttribute("samlAttributes", samlAttributes);
    return "login";
  }
}
