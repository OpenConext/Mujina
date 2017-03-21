package mujina.api;

import mujina.api.SharedController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

@RestController
@RequestMapping(path = "/idp/api" ,consumes = "application/json")
@ConditionalOnProperty(prefix = "idp.api", name = "enabled")
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

  @PutMapping("/attributes/{name:.+}" )
  public void setAttribute(@PathVariable String name, @RequestBody List<String> values) {
    LOG.debug("Request to set attribute {} to {}", name, values);
    configuration().getAttributes().put(name, values);
  }

  @RequestMapping(value = { "/attributes/{name:.+}" }, method = RequestMethod.DELETE)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @ResponseBody
  public void removeAttribute(@PathVariable String name) {
    LOG.debug("Request to remove attribute {}", name);
    configuration().getAttributes().remove(name);
  }

  @RequestMapping(value = { "/users" }, method = RequestMethod.PUT)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @ResponseBody
  public void addUser(@RequestBody User user) {
    LOG.debug("Request to add user {}", user);
    configuration().getUsers().add(new UsernamePasswordAuthenticationToken(
      user.getName(),
      user.getPassword(),
      user.getAuthorities().stream().map(SimpleGrantedAuthority::new).collect(toList())));
  }

  @PutMapping
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
