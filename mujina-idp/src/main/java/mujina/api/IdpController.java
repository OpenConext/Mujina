package mujina.api;

import mujina.idp.FederatedUserAuthenticationToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.*;

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
        LOG.info("Request to replace all attributes {}", attributes);
        configuration().setAttributes(attributes);
    }

    @PutMapping("/attributes/{name:.+}")
    public void setAttribute(@PathVariable String name, @RequestBody List<String> values) {
        LOG.info("Request to set attribute {} to {}", name, values);
        configuration().getAttributes().put(name, values);
    }

    @PutMapping("/attributes/{name:.+}/{userName:.+}")
    public void setAttributeForUser(@PathVariable String name, @PathVariable String userName,
                                    @RequestBody List<String> values) {
        LOG.info("Request to set attribute {} to {} for user {}", name, values, userName);
        configuration().getUsers().stream().filter(userAuthenticationToken -> userAuthenticationToken.getName().equals
                (userName)).findFirst().orElseThrow(() -> new IllegalArgumentException(String.format("User %s first " +
                "must be created", userName))).getAttributes().put(name, values);
    }

    @DeleteMapping("/attributes/{name:.+}")
    public void removeAttribute(@PathVariable String name) {
        LOG.info("Request to remove attribute {}", name);
        configuration().getAttributes().remove(name);
    }

    @DeleteMapping("/attributes/{name:.+}/{userName:.+}")
    public void removeAttributeForUser(@PathVariable String name, @PathVariable String userName) {
        LOG.info("Request to remove attribute {} for user {}", name, userName);
        configuration().getUsers().stream().filter(userAuthenticationToken -> userAuthenticationToken.getName().equals
                (userName)).findFirst().orElseThrow(() -> new IllegalArgumentException(String.format("User %s first " +
                "must be created", userName))).getAttributes().remove(name);
    }

    @GetMapping("/users")
    public List<FederatedUserAuthenticationToken> users() {
        LOG.info("Request to get users");
        return configuration().getUsers();
    }

    @PutMapping("/users")
    public void addUser(@RequestBody User user) {
        LOG.info("Request to add user {}", user);
        FederatedUserAuthenticationToken userAuthenticationToken = new FederatedUserAuthenticationToken(
                user.getName(),
                user.getPassword(),
                user.getAuthorities().stream().map(SimpleGrantedAuthority::new).collect(toList()));
        userAuthenticationToken.getAttributes().putAll(configuration().getAttributes());
        configuration().getUsers().add(userAuthenticationToken);
    }

    @PutMapping("authmethod")
    public void setAuthenticationMethod(@RequestBody String authenticationMethod) {
        LOG.info("Request to set auth method to {}", authenticationMethod);
        configuration().setAuthenticationMethod(AuthenticationMethod.valueOf(authenticationMethod));
    }

    @PutMapping("/acsendpoint")
    public void setAcsEndpoint(@RequestBody String acsEndpoint) {
        LOG.info("Request to set Assertion Consumer Service Endpoint to {}", acsEndpoint);
        configuration().setAcsEndpoint(acsEndpoint);
    }

    private IdpConfiguration configuration() {
        return IdpConfiguration.class.cast(super.configuration);
    }


}
