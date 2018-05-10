package mujina.api;

import mujina.AbstractIntegrationTest;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static mujina.api.AuthenticationMethod.ALL;
import static mujina.api.AuthenticationMethod.USER;
import static org.apache.http.HttpStatus.SC_OK;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class IdpControllerTest extends AbstractIntegrationTest {

  private static final String NEW_ATTRIBUTE = "urn:mace:dir:attribute-def:new";
  private static final String UID_ATTRIBUTE = "urn:mace:dir:attribute-def:uid";

  @Test
  public void setAttributes() {
    List<String> values = Arrays.asList("value1", "value2");
    Map<String, List<String>> attributes = Collections.singletonMap(NEW_ATTRIBUTE, values);

    api(attributes, "/api/attributes/");

    assertEquals(values, idpConfiguration.getAttributes().get(NEW_ATTRIBUTE));
  }

  @Test
  public void setAttribute() {
    List<String> values = Arrays.asList("value1", "value2");

    api(values, "/api/attributes/" + NEW_ATTRIBUTE);

    assertEquals(values, idpConfiguration.getAttributes().get(NEW_ATTRIBUTE));
  }

  @Test
  public void removeAttribute() {
    assertEquals(Arrays.asList("john.doe"), idpConfiguration.getAttributes().get(UID_ATTRIBUTE));

    given()
      .header("Content-Type", "application/json")
      .delete("/api/attributes/urn:mace:dir:attribute-def:uid")
      .then()
      .statusCode(SC_OK);

    assertFalse(idpConfiguration.getAttributes().containsKey(UID_ATTRIBUTE));
  }

  @Test
  public void addUser() {
    User user = new User("Bob", "secret", Arrays.asList("USER", "ADMIN"));
    api(user, "/api/users");

    assertTrue(idpConfiguration.getUsers().stream()
      .filter(token -> token.getName().equals(user.getName())).findAny().isPresent());

  }

  @Test
  public void setAuthenticationMethod() throws Exception {
    assertEquals(USER, idpConfiguration.getAuthenticationMethod());

    api(ALL.name(), "/api/authmethod");

    assertEquals(ALL, idpConfiguration.getAuthenticationMethod());
  }

  @Test
  public void setAcsEndpoint() {
    assertNull(idpConfiguration.getAcsEndpoint());

    String acs = "https://localhost:8080/acs";
    api(acs, "/api/acsendpoint");

    assertEquals(acs, idpConfiguration.getAcsEndpoint());
  }

  @Test
  public void setAttributeForUser() {
    User user = new User("hacker", "secret", Arrays.asList("USER"));
    api(user, "/api/users");

    List<String> values = Arrays.asList("value1", "value2");

    api(values, "/api/attributes/" + NEW_ATTRIBUTE + "/hacker");

    assertEquals(values, idpConfiguration.getUsers().stream()
      .filter(userAuthenticationToken -> userAuthenticationToken.getPrincipal().equals("hacker")).findFirst().get()
      .getAttributes().get(NEW_ATTRIBUTE));
  }

  @Test
  public void removeAttributeForUser() {
    this.setAttributeForUser();

    given()
      .header("Content-Type", "application/json")
      .delete("/api/attributes/" + NEW_ATTRIBUTE + "/hacker")
      .then()
      .statusCode(SC_OK);

    assertFalse(idpConfiguration.getUsers().stream()
      .filter(userAuthenticationToken -> userAuthenticationToken.getPrincipal().equals("hacker")).findFirst().get()
      .getAttributes().containsKey(NEW_ATTRIBUTE));
  }

  private void api(Object body, String path) {
    given()
      .body(body)
      .header("Content-Type", "application/json")
      .put(path)
      .then()
      .statusCode(SC_OK);
  }

}
