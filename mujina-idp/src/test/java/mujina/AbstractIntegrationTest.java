package mujina;

import io.restassured.RestAssured;
import io.restassured.filter.cookie.CookieFilter;
import mujina.api.IdpConfiguration;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static io.restassured.RestAssured.given;
import static org.apache.http.HttpStatus.SC_OK;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {"idp.auth_method=USER"})
public abstract class AbstractIntegrationTest {

  @Autowired
  protected IdpConfiguration idpConfiguration;

  @LocalServerPort
  protected int serverPort;

  @Before
  public void before() throws Exception {
    RestAssured.port = serverPort;
    given()
      .header("Content-Type", "application/json")
      .post("/api/reset")
      .then()
      .statusCode(SC_OK);
  }

  protected CookieFilter login(String username, String password, int statusCode) throws Exception {
    CookieFilter cookieFilter = new CookieFilter();

    given()
      .formParam("username", username)
      .formParam("password", password)
      .filter(cookieFilter)
      .post("/login")
      .then()
      .statusCode(statusCode);

    return cookieFilter;
  }


}
