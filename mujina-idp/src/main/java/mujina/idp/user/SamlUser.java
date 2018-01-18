package mujina.idp.user;

import java.util.HashMap;
import java.util.Map;

public class SamlUser {
  private String username;
  private String password;
  private final Map<String, String> samlAttributes;

  public SamlUser() {
    this.samlAttributes = new HashMap<>();
  }

  public SamlUser(String username, String password, Map<String, String> samlAttributes) {
    this.username = username;
    this.password = password;
    this.samlAttributes = samlAttributes;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public Map<String, String> getSamlAttributes() {
    return samlAttributes;
  }

  @Override
  public String toString() {
    return String.format("SamlUser{username='%s', password='%s', samlAttributes=%s}", username, password, samlAttributes);
  }
}
