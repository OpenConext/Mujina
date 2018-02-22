package mujina.idp.user;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

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

  public Map<String, String> getUserAttributes(String username) {
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
    Optional<SamlUser> samlUserOpt = getSamlUser(username);

    if (samlUserOpt.isPresent()) {
      return samlUserOpt.get().getSamlAttributes();
    } else {
      return new HashMap<>();
    }
  }

  public void setUserAttributes(String username, String attributeName, List<String> attributes) {
    Optional<SamlUser> samlUserOpt = getSamlUser(username);
    String attributeValues = attributesToString(attributes);

    samlUserOpt.ifPresent(samlUser -> samlUser.getSamlAttributes().put(attributeName, attributeValues));
  }

  private String attributesToString(List<String> attributes) {
    return attributes.stream().collect(Collectors.joining(","));
  }

  public void setUserAttributes(String username, Map<String, List<String>> attributes) {
    Optional<SamlUser> samlUserOpt = getSamlUser(username);

    samlUserOpt.ifPresent(
      samlUser -> {
        samlUser.getSamlAttributes().clear();

        // todo try and collect or use a flatMap operation
        attributes.entrySet().stream()
          .map(entry -> Collections.singletonMap(entry.getKey(), attributesToString(entry.getValue())))
          .forEach(attributesMap -> samlUser.getSamlAttributes().putAll(attributesMap));
      });
  }

  public void removeUserAttribute(String username, String attributeName) {
    Optional<SamlUser> samlUserOpt = getSamlUser(username);
    samlUserOpt.ifPresent(samlUser -> samlUser.getSamlAttributes().remove(attributeName));
  }

  private Optional<SamlUser> getSamlUser(String username) {
    return samlUsers.stream()
      .filter(samlUser -> samlUser.getUsername().equals(username))
      .findFirst();
  }

  public void addDynamicUser(String username, String password) {
    if (samlUsers.stream().noneMatch(samlUser -> samlUser.getUsername().equals(username))) {
      samlUsers.add(new SamlUser(username, password, true));
    }
  }

  public void resetDynamicUsers() {
    List<SamlUser> nonDynamicUsers = samlUsers.stream()
      .filter(samlUser -> !samlUser.isDynamic())
      .collect(Collectors.toList());

    samlUsers.retainAll(nonDynamicUsers);
  }

  public void resetSeedDataUserAttributes() {
    samlUsers.stream()
      .filter(samlUser -> !samlUser.isDynamic())
      .forEach(samlUser -> samlUser.getSamlAttributes().clear());
  }
}
