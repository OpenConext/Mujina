package mujina.api;

import io.restassured.RestAssured;
import mujina.AbstractIntegrationTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static mujina.api.AuthenticationMethod.ALL;
import static mujina.api.AuthenticationMethod.USER;
import static org.apache.http.HttpStatus.SC_OK;
import static org.junit.Assert.*;

public class IdpControllerTest extends AbstractIntegrationTest {

  private static final String NEW_ATTRIBUTE = "urn:mace:dir:attribute-def:new";
  private static final String UID_ATTRIBUTE = "urn:mace:dir:attribute-def:uid";

  @Test
  public void setAttributes() throws Exception {
    List<String> values = Arrays.asList("value1", "value2");
    Map<String, List<String>> attributes = Collections.singletonMap(NEW_ATTRIBUTE, values);

    start(attributes, "/api/attributes/");

    assertEquals(values, idpConfiguration.getAttributes().get(NEW_ATTRIBUTE));
  }

  @Test
  public void setAttribute() throws Exception {
    List<String> values = Arrays.asList("value1", "value2");

    start(values, "/api/attributes/" + NEW_ATTRIBUTE);

    assertEquals(values, idpConfiguration.getAttributes().get(NEW_ATTRIBUTE));
  }

  @Test
  public void removeAttribute() throws Exception {
    assertEquals(Arrays.asList("john.doe"), idpConfiguration.getAttributes().get(UID_ATTRIBUTE));

    given()
      .header("Content-Type", "application/json")
      .delete("/api/attributes/urn:mace:dir:attribute-def:uid")
      .then()
      .statusCode(SC_OK);

    assertFalse(idpConfiguration.getAttributes().containsKey(UID_ATTRIBUTE));
  }

  @Test
  public void addUser() throws Exception {
    User user = new User("Bob", "secret", Arrays.asList("USER", "ADMIN"));
    start(user, "/api/users");

    assertTrue(idpConfiguration.getUsers().stream()
      .filter(token -> token.getName().equals(user.getName())).findAny().isPresent());

  }

  @Test
  public void setAuthenticationMethod() throws Exception {
    assertEquals(USER, idpConfiguration.getAuthenticationMethod());

    start(ALL.name(), "/api/authmethod");

    assertEquals(ALL, idpConfiguration.getAuthenticationMethod());
  }

  @Test
  public void setAcsEndpoint() throws Exception {
    assertNull(idpConfiguration.getAcsEndpoint());

    String acs = "https://localhost:8080/acs";
    start(acs, "/api/acsendpoint");

    assertEquals(acs, idpConfiguration.getAcsEndpoint());
  }

  private void start(Object body, String path) {
    given()
      .body(body)
      .header("Content-Type", "application/json")
      .put(path)
      .then()
      .statusCode(SC_OK);
  }

}
