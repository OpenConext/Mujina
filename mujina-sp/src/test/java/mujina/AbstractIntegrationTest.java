package mujina;

import io.restassured.RestAssured;
import io.restassured.filter.cookie.CookieFilter;
import mujina.api.SpConfiguration;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.nio.charset.Charset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.restassured.RestAssured.given;
import static org.apache.http.HttpStatus.SC_MOVED_TEMPORARILY;
import static org.apache.http.HttpStatus.SC_OK;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class AbstractIntegrationTest {

  @Autowired
  protected SpConfiguration spConfiguration;

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

  protected CookieFilter login() throws IOException {
    CookieFilter cookieFilter = new CookieFilter();

    String html = given()
      .filter(cookieFilter)
      .get("/login")
      .getBody().asString();

    Matcher matcher = Pattern.compile("name=\"SAMLRequest\" value=\"(.*?)\"").matcher(html);
    matcher.find();
    String samlRequest = new String(Base64.getDecoder().decode(matcher.group(1)));

    //Now mimic a response message
    String samlResponse = getIdPSAMLResponse(samlRequest);
    given()
      .formParam("SAMLResponse", Base64.getEncoder().encodeToString(samlResponse.getBytes()))
      .filter(cookieFilter)
      .post("/saml/SSO")
      .then()
      .statusCode(SC_MOVED_TEMPORARILY);

    return cookieFilter;
  }

  private String getIdPSAMLResponse(String saml) throws IOException {
    Matcher matcher = Pattern.compile("ID=\"(.*?)\"").matcher(saml);
    assertTrue(matcher.find());

    //We need the ID of the original request to mimic the real IdP authnResponse
    String inResponseTo = matcher.group(1);

    ZonedDateTime date = ZonedDateTime.now();
    String now = date.format(DateTimeFormatter.ISO_INSTANT);
    String samlResponse = IOUtils.toString(new ClassPathResource("saml_response.xml").getInputStream(),Charset.defaultCharset());

    samlResponse = samlResponse
      .replaceAll("@@IssueInstant@@", now)
      .replaceAll("@@InResponseTo@@", inResponseTo);
    return samlResponse;
  }

}
