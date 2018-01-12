package mujina.idp.user;

import java.util.*;


public class SamlUserAttributeStore {
  private List<SamlUser> samlUsers;

  public SamlUserAttributeStore(List<SamlUser> samlUsers) {
    this.samlUsers = samlUsers;
  }

  public Map<String, List<String>> getAttributesByUsername(String username) {
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
