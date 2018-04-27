package mujina.api;

import mujina.idp.FederatedUserAuthenticationToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

@RestController
@RequestMapping(path = "/api", consumes = "application/json")
public class IdpController extends SharedController {

  @Autowired
  public IdpController(IdpConfiguration configuration) {
    super(configuration);
  }

  @PutMapping("/attributes")
  public void setAttributes(@RequestBody Map<String, List<String>> attributes) {
    LOG.debug("Request to replace all attributes {}", attributes);
    configuration().setAttributes(attributes);
  }

  @PutMapping("/attributes/{name:.+}")
  public void setAttribute(@PathVariable String name, @RequestBody List<String> values) {
    LOG.debug("Request to set attribute {} to {}", name, values);
    configuration().getAttributes().put(name, values);
  }

  @PutMapping("/attributes/{name:.+}/{userName:.+}")
  public void setAttributeForUser(@PathVariable String name, @PathVariable String userName,
                                  @RequestBody List<String> values) {
    LOG.debug("Request to set attribute {} to {}", name, values);
    configuration().getUsers().stream().filter(userAuthenticationToken -> userAuthenticationToken.getName().equals
      (userName)).findFirst().orElseThrow(() -> new IllegalArgumentException(String.format("User %s first " +
      "must be created", userName))).getAttributes().put(name, values);
  }

  @DeleteMapping("/attributes/{name:.+}")
  public void removeAttribute(@PathVariable String name) {
    LOG.debug("Request to remove attribute {}", name);
    configuration().getAttributes().remove(name);
  }

  @PutMapping("/users")
  public void addUser(@RequestBody User user) {
    LOG.debug("Request to add user {}", user);
    FederatedUserAuthenticationToken userAuthenticationToken = new FederatedUserAuthenticationToken(
      user.getName(),
      user.getPassword(),
      user.getAuthorities().stream().map(SimpleGrantedAuthority::new).collect(toList()));
    userAuthenticationToken.setAttributes(configuration().getAttributes());
    configuration().getUsers().add(userAuthenticationToken);
  }

  @PutMapping("authmethod")
  public void setAuthenticationMethod(@RequestBody String authenticationMethod) {
    LOG.debug("Request to set auth method to {}", authenticationMethod);
    configuration().setAuthenticationMethod(AuthenticationMethod.valueOf(authenticationMethod));
  }

  @PutMapping("/acsendpoint")
  public void setAcsEndpoint(@RequestBody String acsEndpoint) {
    LOG.debug("Request to set Assertion Consumer Service Endpoint to {}", acsEndpoint);
    configuration().setAcsEndpoint(acsEndpoint);
  }

  private IdpConfiguration configuration() {
    return IdpConfiguration.class.cast(super.configuration);
  }


}
