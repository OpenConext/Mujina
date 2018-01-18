package mujina.idp.user;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.*;

import static java.util.Collections.singletonMap;

@Component
@ConfigurationProperties("samlUserStore")
public class SamlUserStore {
  private final List<SamlUser> samlUsers;

  public SamlUserStore() {
    this.samlUsers = new ArrayList<>();
  }

  SamlUserStore(List<SamlUser> samlUsers) {
    this.samlUsers = samlUsers;
  }

  public List<SamlUser> getSamlUsers() {
    return samlUsers;
  }

  public Map<String, String> getAttributesByUsername(String username) {
    switch (username) {
      case "admin":
      case "user":
        return singletonMap("attribute 1", String.format("%s attribute 1", username));
      default:
        return getSamlUserAttributes(username);
    }
  }

  private Map<String, String> getSamlUserAttributes(String username) {
    // todo consider using a flat map to get all attributes for a particular username, not just the first
    Optional<SamlUser> samlUserOpt = samlUsers.stream()
      .filter(samlUser -> samlUser.getUsername().equals(username))
      .findFirst();

    if (samlUserOpt.isPresent()) {
      return samlUserOpt.get().getSamlAttributes();
    } else {
      return new HashMap<>();
    }
  }
}
