package mujina.api;

import io.restassured.RestAssured;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static java.util.Arrays.asList;
import static org.apache.http.HttpStatus.SC_MOVED_TEMPORARILY;


@RunWith(SpringRunner.class)
@TestPropertySource(properties = {"idp.api_enabled: false"})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DisabledApiTest {

  @LocalServerPort
  private int serverPort;

  @Before
  public void before() throws Exception {
    RestAssured.port = serverPort;
  }

  @Test
  public void shouldDenyAccessToUserApiEndpoint() {
    User user = new User("Bob", "secret", asList("USER", "ADMIN"));

    given()
      .contentType(JSON)
      .body(user)
      .when()
      .put("/api/users")
      .then()
      .statusCode(SC_MOVED_TEMPORARILY); // todo fix the the Spring Security at idp\WebSecurityConfigurer.java:135 so that form based authentication does not apply to the restricted /api url (which results in a 302 to the login page, and not 403 (forbidden)
  }
}
