package mujina.idp;

import io.restassured.filter.cookie.CookieFilter;
import io.restassured.path.xml.XmlPath;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import mujina.AbstractIntegrationTest;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.junit.Assert;
import org.junit.Test;
import org.opensaml.saml2.core.StatusCode;
import org.opensaml.xml.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.TestPropertySource;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.Inflater;

import static io.restassured.RestAssured.given;
import static org.apache.http.HttpStatus.SC_MOVED_TEMPORARILY;
import static org.apache.http.HttpStatus.SC_OK;
import static org.hamcrest.core.StringContains.containsString;

@TestPropertySource(properties = {"idp.expires:" + (Integer.MAX_VALUE / 2 - 1), "idp.clock_skew: " + (Integer.MAX_VALUE / 2 - 1)})
public class UserControllerTest extends AbstractIntegrationTest {

  @LocalServerPort
  private int testPort;

  protected final Logger LOG = LoggerFactory.getLogger(getClass());

  private final String slsEndpoint = "http://localhost:9090/saml/SLS";

  private final List<String[]> params = Arrays.asList(
    new String[]{"SigAlg", "http://www.w3.org/2000/09/xmldsig#rsa-sha1"},
    new String[]{"SAMLRequest", "fVJdS8MwFH3fryh5b5to6WrYCsIQBlPUig++XdJ0C+bL3FT28227oUO25Sm599xz7jlkgWC05xu3dX18lV+9xJjsjbbIp86S9MFyB6iQWzASeRS8uX/c8JuMch9cdMJpMkvOnT+e6zSAKENUzl7gWa+WpBO0m7ddCay4o6KklIqKlUXRkuRdBhyGl2TgusSA2Mu1xQg2DjjKipTOU1a9UcbZLaflB0lWg3VlIU5Uuxg9z3PtBOidw8grWtG8UXar5SGsRoZvJSSpJ8XF6JJPMqE+DhsnPlP0i/y0d4J+GoJYr5Lmeby89KBVp2T41UafyT0Yr2UmnMlbaRzLjYzQQoTM7/wZrw8uGIjX0x4rqk27Ccp7i16KUbklNbRG2eO+h+3q2eH574vUsx8="},
    new String[]{"Signature", "Aj/IPPRSTE17Aa6fJpdoglVFCmjCUA4pw4drtlSkmwwKoYqvXLfjCBmhofAxgqmTkF2m2o188GobNOdccJ2FQu0APJalznp41uLZAUbQsyCfY5K53V5w5A7gDsJfVBM0ajgSYtKai+ZgPqE+qr0vWeF2E5HBqxLx3ui8IGT+GBo="}
  );

  private final List<String[]> formParams = Collections.singletonList(
    new String[]{"SAMLRequest", "PD94bWwgdmVyc2lvbj0iMS4wIj8+CjxzYW1scDpMb2dvdXRSZXF1ZXN0IHhtbG5zOnNhbWxwPSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6cHJvdG9jb2wiIHhtbG5zOnNhbWw9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDphc3NlcnRpb24iIElEPSJwZng2ZTZkYjg2OS1hNmQ0LTM3NTUtMGNiYy00ZTEwODQyNTE1NzciIFZlcnNpb249IjIuMCIgSXNzdWVJbnN0YW50PSIyMDE0LTA3LTE4VDAxOjEzOjA2WiIgRGVzdGluYXRpb249Imh0dHA6Ly9sb2NhbGhvc3Q6ODA4MC9TaW5nbGVMb2dvdXRTZXJ2aWNlIj4KICAgIDxzYW1sOklzc3Vlcj5odHRwOi8vbW9jay1zcDwvc2FtbDpJc3N1ZXI+PGRzOlNpZ25hdHVyZSB4bWxuczpkcz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC8wOS94bWxkc2lnIyI+CiAgPGRzOlNpZ25lZEluZm8+PGRzOkNhbm9uaWNhbGl6YXRpb25NZXRob2QgQWxnb3JpdGhtPSJodHRwOi8vd3d3LnczLm9yZy8yMDAxLzEwL3htbC1leGMtYzE0biMiLz4KICAgIDxkczpTaWduYXR1cmVNZXRob2QgQWxnb3JpdGhtPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwLzA5L3htbGRzaWcjcnNhLXNoYTEiLz4KICA8ZHM6UmVmZXJlbmNlIFVSST0iI3BmeDZlNmRiODY5LWE2ZDQtMzc1NS0wY2JjLTRlMTA4NDI1MTU3NyI+PGRzOlRyYW5zZm9ybXM+PGRzOlRyYW5zZm9ybSBBbGdvcml0aG09Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvMDkveG1sZHNpZyNlbnZlbG9wZWQtc2lnbmF0dXJlIi8+PGRzOlRyYW5zZm9ybSBBbGdvcml0aG09Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvMTAveG1sLWV4Yy1jMTRuIyIvPjwvZHM6VHJhbnNmb3Jtcz48ZHM6RGlnZXN0TWV0aG9kIEFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvMjAwMC8wOS94bWxkc2lnI3NoYTEiLz48ZHM6RGlnZXN0VmFsdWU+WVIxYVp0ZUhuY0swelhoUTFwd2ZQZkJCVTVNPTwvZHM6RGlnZXN0VmFsdWU+PC9kczpSZWZlcmVuY2U+PC9kczpTaWduZWRJbmZvPjxkczpTaWduYXR1cmVWYWx1ZT5ESDJlS3lQQ2prN3VGOTdqNmcyNGRIbXNUN1hTa1Jvb3lYT2ZnMFAxek1ZbVU2emZyWkU4TXFYdjRmSVQrQ3Irc2YzRjd0TjFFZDBVNXhkWjNYZ0RxWXcrekNJRis2aXQyOC9rcG52c3R4anA2V2Y1RFozYUdock5TWVRxbFJWQVhjOXJDbWtlaVQ4UVZOdWtqbmttNTVvaDlyM0tPL2lMQml1Yk1oeUVHRzFCM21nemR1L3RnU2c0MGxGcjBEa2NEMmtrb3VEZFd5eGUzcTFGUUExZ3g5dW5vTG5GYTFOQ2p2b25rZ2YyTnExdVUyaklsSk1rNnJ5ZHRYMkpQbkY4TmxvS0RsdDJlKzM3emRnODI3YkZlTG05cUU2NGdBekp1UW5FUnFIQ291RVNEZzVJelBRVEFiaWVuQzdqMUJNWG5OYmRxbURmUzNkckswSDlxcWQ2OHc9PTwvZHM6U2lnbmF0dXJlVmFsdWU+CjxkczpLZXlJbmZvPjxkczpYNTA5RGF0YT48ZHM6WDUwOUNlcnRpZmljYXRlPk1JSURFekNDQWZ1Z0F3SUJBZ0lKQUtvSy9oZUJqY09ZTUEwR0NTcUdTSWIzRFFFQkJRVUFNQ0F4SGpBY0JnTlZCQW9NRlU5eVoyRnVhWHBoZEdsdmJpd2dRMDQ5VDBsRVF6QWVGdzB4TlRFeE1URXhNREV5TVRWYUZ3MHlOVEV4TVRBeE1ERXlNVFZhTUNBeEhqQWNCZ05WQkFvTUZVOXlaMkZ1YVhwaGRHbHZiaXdnUTA0OVQwbEVRekNDQVNJd0RRWUpLb1pJaHZjTkFRRUJCUUFEZ2dFUEFEQ0NBUW9DZ2dFQkFOQkd3Si9xcFRRTmlTZ1VnbFNFMlV6RWtVb3crd1M4cjY3ZXR4b0VobHpKWmZnSy9rNVRmRzF3SUNEcWFwSEF4RVZnVU0xMGFCSFJjdE5vY0E1d21sSHR4ZGlkaHpSWnJvcUh3cEt5MkJtc0tYNVoyb0syNVJMcHN5dXNCMUtyb2VtZ0EvQ2pVbkk2cklMMXh4Rm4zS3lPRmgxWkJMVVF0S05RZU1TN0hGR2dTREFwK3NYdVRGdWp6MTJMRkR1Z1gwVDBLQjVhMSswbDh5MFBFYTB5R2Exb2k2c2VPTng4NDlaSHhNMFBSdlV1bldrdVRNK2ZvWjBqWnBGYXBYZTAyeVdNcWhjLzJpWU1pZUUvM0d2T2d1SmNoSnQ2UitjdXQ4VkJiNnViS1VJR0s3cG1vcS9UQjZEVlhwdnNIcXNESlhlY2h4Y2ljdTRwZEtWREhTZWM4NTBDQXdFQUFhTlFNRTR3SFFZRFZSME9CQllFRks3UnFqb29kU1lWWEdUVkVkTGYza0pmbFAvc01COEdBMVVkSXdRWU1CYUFGSzdScWpvb2RTWVZYR1RWRWRMZjNrSmZsUC9zTUF3R0ExVWRFd1FGTUFNQkFmOHdEUVlKS29aSWh2Y05BUUVGQlFBRGdnRUJBRE5aa3hsRlhoNEY0NW11Q2JuUWQrV21hWGxHdmI5dGtVeUFJeFZMOEFJdThKMThGNDIwdnBuR3BvVUFFK0h5M2V2Qm1wMm5rckZBZ21yMDU1ZkFqcEhlWkZnRFpCQVBDd1lkM1ROTURlU3lNdGEzS2Erb1M3R1JGRGVQa01FbStrSDQvcklUTktVRjFzT3ZXQlRTb3drOVR1ZEVEeUZxZ0dudGNkdS9sL3pSeHZ4MzN5M0xNRzVVU0QweDRYNElLalJyUk4xQmJjS2dpOGRxMTBDM2pkcU5hbmNUdVBvcVQzV1d6UnZWdEIvcTM0QjdGNzQvNkp6Z0VvT0NFSHVmQk1wNFpGdTU0UDB5RUd0V2ZUd1R6dW9ab2JyQ2hWVkJ0NHcvWFphZ3JSdFVDRE53UnBITmJwanhZdWRicUxxcGkxTVFwVjlvaHQvQnBUSFZKRzJpMHJvPTwvZHM6WDUwOUNlcnRpZmljYXRlPjwvZHM6WDUwOURhdGE+PC9kczpLZXlJbmZvPjwvZHM6U2lnbmF0dXJlPgogICAgPHNhbWw6TmFtZUlEIFNQTmFtZVF1YWxpZmllcj0iaHR0cDovL3NwLmV4YW1wbGUuY29tL2RlbW8xL21ldGFkYXRhLnBocCIgRm9ybWF0PSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6bmFtZWlkLWZvcm1hdDp1bnNwZWNpZmllZCI+YWRtaW48L3NhbWw6TmFtZUlEPgo8L3NhbWxwOkxvZ291dFJlcXVlc3Q+Cg=="}
  );

  @Test
  public void user() throws Exception {
    doUser("/user.html");
  }

  @Test
  public void index() throws Exception {
    doUser("/");
  }

  @Test
  public void indexNotLoggedIn() {
    given()
      .get("/")
      .then()
      .statusCode(SC_OK)
      .body(containsString("Login"));
  }

  private void doUser(String path) throws Exception {
    CookieFilter cookieFilter = login("admin", "secret", SC_MOVED_TEMPORARILY);
    doUser(path, cookieFilter);
  }

  private void doUser(String path, CookieFilter cookieFilter) {
    given()
      .filter(cookieFilter)
      .get(path)
      .then()
      .statusCode(SC_OK)
      .body(containsString("admin"));
  }

  @Test
  public void logoutGet() throws Exception {
    doLogout(false);
  }

  @Test
  public void logoutPost() throws Exception {
    doLogout(true);
  }

  private void doLogout(boolean post) throws Exception {

    // we need to set some ACS endpoint for logout to work
    given()
      .header("content-type", "application/json")
      .body(slsEndpoint)
      .put("/api/slsendpoint")
      .then()
      .statusCode(SC_OK);

    // let's login before we logout.
    CookieFilter cookieFilter = login("admin", "secret", SC_MOVED_TEMPORARILY);
    doUser("/user.html", cookieFilter);

    // test log out
    doSLO(post, cookieFilter, true);
    // test logout endpoint with logged out user
    doSLO(post, cookieFilter, false);

  }

  private void doSLO(boolean post, CookieFilter cookieFilter, boolean ok) throws Exception {

    RequestSpecification requestSpecification = given();
    if (post) {
      formParams.forEach(param -> requestSpecification.formParam(param[0], param[1]));
    } else {
      params.forEach(param -> requestSpecification.param(param[0], param[1]));
    }
    requestSpecification
      .filter(cookieFilter)
      // we need to not let HTTP client redirect, otherwise it will
      // attempt to access fake SP SLS URL which has no service
      .when().redirects().follow(false);

    String path = "/SingleLogoutService";
    Response response = post ? requestSpecification.post(path) : requestSpecification.get(path);

    response
      .then()
      .statusCode(SC_MOVED_TEMPORARILY);

    LOG.info("Redirect to:"+response.getHeader("location"));

    URI redirectURI = new URI(response.getHeader("location"));

    if (ok) {
      // we must receive a redirect request to SLS URL with SAMLResponse query parameter

      List<NameValuePair> params = URLEncodedUtils.parse(redirectURI, StandardCharsets.UTF_8);
      Map<String, String> pMap = new HashMap<>();
      for (NameValuePair nvp : params) {
        pMap.put(nvp.getName(), nvp.getValue());
      }
      String samlResponse = pMap.get("SAMLResponse");
      XmlPath xmlPath = new XmlPath(getDeflatedResponse(samlResponse));

      Assert.assertEquals(StatusCode.SUCCESS_URI, xmlPath.get("LogoutResponse.Status.StatusCode.@Value"));
      Assert.assertEquals(slsEndpoint, xmlPath.get("LogoutResponse.@Destination"));

    } else {

      // we should be redirected to our own login
      Assert.assertEquals("http://localhost:"+testPort+"/login", redirectURI.toString());

    }

  }

  private String getDeflatedResponse(String input) throws Exception {

    byte[] decoded = Base64.decode(input);

    Inflater inflater = new Inflater(true);
    inflater.setInput(decoded);

    ByteArrayOutputStream buf = new ByteArrayOutputStream();

    while (!inflater.finished()) {
      byte [] chunk = new byte[1024];
      int bytes = inflater.inflate(chunk);
      buf.write(chunk, 0, bytes);
    }

    return new String(buf.toByteArray(), StandardCharsets.UTF_8);

  }

}
