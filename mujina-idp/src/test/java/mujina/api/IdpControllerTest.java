package mujina.api;

import mujina.AbstractIntegrationTest;
import mujina.idp.user.SamlUser;
import mujina.idp.user.SamlUserStore;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static java.util.Arrays.asList;
import static mujina.api.AuthenticationMethod.ALL;
import static mujina.api.AuthenticationMethod.USER;
import static org.apache.http.HttpStatus.SC_OK;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class IdpControllerTest extends AbstractIntegrationTest {

  private static final String TEST_USERNAME = "a test user";
  private static final String TEST_ATTRIBUTE = "urn:mace:dir:attribute-def:test";
  private static final String TEST_ATTRIBUTE_2 = "urn:mace:dir:attribute-def:test-2";
  private static final String UID_ATTRIBUTE = "urn:mace:dir:attribute-def:uid";

  @Autowired
  private SamlUserStore samlUserStore;

  @Before
  public void setup() {
    samlUserStore.getSamlUsers().clear();
    HashMap<String, String> map = new HashMap<>();
    map.put("test attribute", "test value");
    samlUserStore.getSamlUsers().add(new SamlUser(TEST_USERNAME, "test password", map));

    idpConfiguration.reset();
  }

  @Test
  public void shouldSetMultipleAttributesOnaUser() throws Exception {
    // given
    List<String> values = asList("value1", "value2");
    List<String> values2 = asList("value3", "value4");

    Map<String, List<String>> attributes = new HashMap<>();
    attributes.put(TEST_ATTRIBUTE, values);
    attributes.put(TEST_ATTRIBUTE_2, values2);

    // when
    api(attributes, String.format("/api/attributes/%s", TEST_USERNAME));

    // then
    Map<String, String> userAttributes = idpConfiguration.getUserAttributes(TEST_USERNAME);

    assertThat(userAttributes.size(), is(2));
    assertThat(userAttributes.get(TEST_ATTRIBUTE), is("value1,value2"));
    assertThat(userAttributes.get(TEST_ATTRIBUTE_2), is("value3,value4"));
  }

  @Test
  public void setAttribute() throws Exception {
    // given
    List<String> values = asList("value1", "value2");

    // when
    api(values, String.format("/api/attributes/%s/%s", TEST_USERNAME, TEST_ATTRIBUTE));

    // then
    Map<String, String> userAttributes = idpConfiguration.getUserAttributes(TEST_USERNAME);
    assertThat(userAttributes.get(TEST_ATTRIBUTE), is("value1,value2"));
  }

  @Test
  public void removeAttribute() throws Exception {
    // given
    Map<String, String> userAttributes = idpConfiguration.getUserAttributes(TEST_USERNAME);
    userAttributes.put(UID_ATTRIBUTE, "john.doe");
    assertThat(userAttributes.get(UID_ATTRIBUTE), is("john.doe"));

    // when
    given()
      .header("Content-Type", "application/json")
      .delete(String.format("/api/attributes/%s/%s", TEST_USERNAME, UID_ATTRIBUTE))
      .then()
      .statusCode(SC_OK);

    // then
    assertThat(userAttributes.containsKey(UID_ATTRIBUTE), is(false));
  }

  @Test
  public void addUser() throws Exception {
    User user = new User("Bob", "secret", asList("USER", "ADMIN"));
    api(user, "/api/users");

    assertTrue(idpConfiguration.getUsers().stream()
      .anyMatch(token -> token.getName().equals(user.getName())));
  }

  @Test
  public void shouldOnlyAddUserOnce() throws Exception {
    User user = new User("Bob", "secret", asList("USER", "ADMIN"));
    api(user, "/api/users");

    assertTrue(idpConfiguration.getUsers().stream()
      .anyMatch(token -> token.getName().equals(user.getName())));

    api(user, "/api/users");

    int userCount = idpConfiguration.getUsers().stream()
      .filter(token -> token.getPrincipal().equals(user.getName()))
      .collect(Collectors.toList())
      .size();

    assertThat(userCount, is(1));
  }

  @Test
  public void setAuthenticationMethod() throws Exception {
    assertEquals(USER, idpConfiguration.getAuthenticationMethod());

    api(ALL.name(), "/api/authmethod");

    assertEquals(ALL, idpConfiguration.getAuthenticationMethod());
  }

  @Test
  public void setAcsEndpoint() throws Exception {
    assertNull(idpConfiguration.getAcsEndpoint());

    String acs = "https://localhost:8080/acs";
    api(acs, "/api/acsendpoint");

    assertEquals(acs, idpConfiguration.getAcsEndpoint());
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
