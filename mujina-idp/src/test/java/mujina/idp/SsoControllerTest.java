package mujina.idp;

import io.restassured.filter.cookie.CookieFilter;
import io.restassured.response.Response;
import mujina.AbstractIntegrationTest;
import org.junit.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.restassured.RestAssured.given;
import static org.apache.http.HttpStatus.SC_MOVED_TEMPORARILY;
import static org.apache.http.HttpStatus.SC_OK;
import static org.junit.Assert.assertTrue;

@TestPropertySource(properties = { "idp.expires:" + (Integer.MAX_VALUE / 2 - 1), "idp.clock_skew: " + (Integer.MAX_VALUE / 2 - 1) })
@ActiveProfiles(profiles = "test")
public class SsoControllerTest extends AbstractIntegrationTest {

  @Test
  public void singleSignOnService() throws Exception {
    CookieFilter cookieFilter = login("admin", "secret", SC_MOVED_TEMPORARILY);

    Response response = given()
      .param("SigAlg", "http://www.w3.org/2000/09/xmldsig#rsa-sha1")
      .param("SAMLRequest", "fZFRb4IwFIX/StN3oIIb2AjGzZiZuEgE97C3WitgoGW9xfjzVxU3lyU+3vbce8797nhyamp0FBoqJWM8cAlGQnK1q2QR400+dyI8ScbAmtpv6bQzpVyLr06AQbZRAr3+xLjTkioGFVDJGgHUcJpN35fUdwlttTKKqxqjKYDQxlq9KgldI3Qm9LHiYrNexrg0pqWeVyvO6lKBoSMyIt7ZwMuyFUYz61pJZi5J/4kjEhEvs7FrkVWFXMl+NEZzpbm4RI/xntVgnxazGLMBI8WwfC59vtvZYnsQfsAPBSdhET5ZDaQMoDqK3y6ATiwkGCZNjH0yCB0SOP4wHwR0GNBg5IZB9IlR2u/7Uskrx0dwtlcR0Lc8T510leUYfdzuYQW4p08v7voe++PB7MYaJ2dYYGk13cECdKF1f7i50Ok9V1KcjCvrsXfvlfTl38Mn3w==")
      .param("Signature", "WWDMxCvr5erxB7H4U6BeI5e/l+EtQQUNPixjMLpfGHekfrQ86u7YEoJH8ZgN2bJG4qoWld6fjuh10j8qehp62Qdktxh81iZUySKkt8xGaWEuWMYzZDPdNsucn8HhSzcp0jyeT5ZihShMFO0Dv/KSCBpAt7WKrrZUrATF6cYoz7HsW3hJLh0lLP+Kr6MA6SrtrCxhaYbKhcZ+3wNHoOYQZhbqxIOvfuihRVnCxm8541gmjtHxj3x/qtQRqswOF7k24wUj6/H5Klj/A1/sZh1MZ5jDHfw795YIH3LqpFPzhWl5XEJ+K84vp0RRWaTUOb1hUPlLLWH8tH0Kd4RhaOcNpQ==")
      .filter(cookieFilter)
      .get("/SingleSignOnService");

    response
      .then()
      .statusCode(SC_OK);

    String html = response.getBody().asString();

    Matcher matcher = Pattern.compile("name=\"SAMLResponse\" value=\"(.*?)\"").matcher(html);
    matcher.find();
    String samlResponse = new String(Base64.getDecoder().decode(matcher.group(1)));
    assertTrue(samlResponse.contains("j.doe@example.com"));
  }

}
