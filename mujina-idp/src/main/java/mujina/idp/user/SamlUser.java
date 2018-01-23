package mujina.idp.user;

import java.util.Map;
import java.util.TreeMap;

public class SamlUser {
  private String username;
  private String password;
  private final Map<String, String> samlAttributes;
  private boolean dynamic;

  public SamlUser() {
    this.samlAttributes = new TreeMap<>();
  }

  public SamlUser(String username, String password, boolean dynamic) {
    this();
    this.username = username;
    this.password = password;
    this.dynamic = dynamic;
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

  public boolean isDynamic() {
    return dynamic;
  }

  @Override
  public String toString() {
    return String.format("SamlUser{username='%s', password='%s', samlAttributes=%s, dynamic=%s}", username, password, samlAttributes, dynamic);
  }
}
