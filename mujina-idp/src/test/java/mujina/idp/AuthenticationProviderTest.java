package mujina.idp;

import io.restassured.filter.cookie.CookieFilter;
import mujina.AbstractIntegrationTest;
import mujina.api.AuthenticationMethod;
import org.junit.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static io.restassured.RestAssured.given;
import static org.apache.http.HttpStatus.SC_MOVED_TEMPORARILY;
import static org.apache.http.HttpStatus.SC_OK;
import static org.hamcrest.core.StringContains.containsString;

@TestPropertySource(properties = {"idp.expires:" + (Integer.MAX_VALUE / 2 - 1), "idp.clock_skew: " + (Integer.MAX_VALUE / 2 - 1)})
public class AuthenticationProviderTest extends AbstractIntegrationTest {

  @Test
  public void authenticateAuthMethodAll() throws Exception {

    given()
      .body(AuthenticationMethod.ALL)
      .header("Content-Type", "application/json")
      .put("/api/authmethod")
      .then()
      .statusCode(SC_OK);

    CookieFilter cookieFilter = login("qwerty", "", SC_MOVED_TEMPORARILY);


    given()
      .filter(cookieFilter)
      .get("/user.html")
      .then()
      .statusCode(SC_OK)
      .body(containsString("qwerty"));
  }

  @Test
  public void authenticateWrongPassword() throws Exception {
    given()
      .formParam("username", "admin")
      .formParam("password", "nope")
      .post("/login")
      .then()
      .header("Location", "http://localhost:" + serverPort + "/login?error=true");
  }
}
