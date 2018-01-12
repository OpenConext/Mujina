package mujina.idp.user;

import java.util.List;
import java.util.Map;

public class SamlUser {
  private String username;
  private Map<String, List<String>> samlAttributes;

  public SamlUser(String username, Map<String, List<String>> samlAttributes) {
    this.username = username;
    this.samlAttributes = samlAttributes;
  }

  public String getUsername() {
    return username;
  }

  public Map<String, List<String>> getSamlAttributes() {
    return samlAttributes;
  }
}
