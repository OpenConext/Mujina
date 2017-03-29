package mujina.idp;

import io.restassured.filter.cookie.CookieFilter;
import mujina.AbstractIntegrationTest;
import org.junit.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static io.restassured.RestAssured.given;
import static org.apache.http.HttpStatus.SC_MOVED_TEMPORARILY;
import static org.apache.http.HttpStatus.SC_OK;
import static org.hamcrest.core.StringContains.containsString;

@TestPropertySource(properties = {"idp.expires:" + (Integer.MAX_VALUE / 2 - 1), "idp.clock_skew: " + (Integer.MAX_VALUE / 2 - 1)})
public class UserControllerTest extends AbstractIntegrationTest {

  @Test
  public void user() throws Exception {
    doUser("/user.html");
  }

  @Test
  public void index() throws Exception {
    doUser("/");
  }

  @Test
  public void indexNotLoggedIn() throws Exception {
    given()
      .get("/")
      .then()
      .statusCode(SC_OK)
      .body(containsString("Login"));
  }

  private void doUser(String path) throws Exception {
    CookieFilter cookieFilter = login("admin", "secret", SC_MOVED_TEMPORARILY);

    given()
      .filter(cookieFilter)
      .get(path)
      .then()
      .statusCode(SC_OK)
      .body(containsString("admin"));
  }
}
