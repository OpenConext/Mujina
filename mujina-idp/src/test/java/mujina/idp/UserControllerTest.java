package mujina.idp;

import io.restassured.filter.cookie.CookieFilter;
import io.restassured.response.Response;
import mujina.AbstractIntegrationTest;
import org.junit.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static io.restassured.RestAssured.given;
import static org.apache.http.HttpStatus.SC_MOVED_TEMPORARILY;
import static org.apache.http.HttpStatus.SC_OK;
import static org.hamcrest.core.StringContains.containsString;

@TestPropertySource(properties = {"idp.expires:" + (Integer.MAX_VALUE / 2 - 1), "idp.clock_skew: " + (Integer.MAX_VALUE / 2 - 1)})
@ActiveProfiles(profiles = "test")
public class UserControllerTest extends AbstractIntegrationTest {

  @Test
  public void user() throws Exception {
    CookieFilter cookieFilter = login("admin", "secret", SC_MOVED_TEMPORARILY);

    given()
      .filter(cookieFilter)
      .get("/user.html")
      .then()
      .statusCode(SC_OK)
      .body(containsString("admin"));
  }

}
