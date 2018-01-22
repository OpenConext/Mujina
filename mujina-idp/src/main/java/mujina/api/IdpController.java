package mujina.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(path = "/api", consumes = "application/json")
public class IdpController extends SharedController {

  @Autowired
  public IdpController(IdpConfiguration configuration) {
    super(configuration);
  }

  @PutMapping("/attributes/{username}")
  public void setAttributes(@PathVariable String username, @RequestBody Map<String, List<String>> attributes) {
    LOG.debug("Request to replace all attributes for user '{}' to {}", username, attributes);
    configuration().setUserAttributes(username, attributes);
  }

  @PutMapping("/attributes/{username}/{attributeName:.+}")
  public void setAttribute(@PathVariable String username, @PathVariable String attributeName, @RequestBody List<String> attributeValues) {
    LOG.debug("Request to set {} attribute for user '{}' to {}", attributeName, username, attributeValues);
    configuration().setUserAttributes(username, attributeName, attributeValues);
  }

  @DeleteMapping("/attributes/{username}/{attributeName:.+}")
  public void removeAttribute(@PathVariable String username, @PathVariable String attributeName) {
    LOG.debug("Request to remove attribute {} from user '{}'", attributeName, username);
    configuration().removeUserAttribute(username, attributeName);
  }

  @PutMapping("/users")
  public void addUser(@RequestBody User user) {
    LOG.debug("Request to add user {}", user);
    configuration().addNewUser(user);
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
