package mujina.idp;

import io.restassured.filter.cookie.CookieFilter;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import mujina.AbstractIntegrationTest;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.restassured.RestAssured.given;
import static org.apache.http.HttpStatus.SC_MOVED_TEMPORARILY;
import static org.apache.http.HttpStatus.SC_OK;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

@TestPropertySource(properties = {"idp.expires:" + (Integer.MAX_VALUE / 2 - 1), "idp.clock_skew: " + (Integer.MAX_VALUE / 2 - 1)})
public class SsoControllerTest extends AbstractIntegrationTest {

  private List<String[]> params = Arrays.asList(
    new String[]{"SigAlg", "http://www.w3.org/2001/04/xmldsig-more#rsa-sha256"},
    new String[]{"SAMLRequest", "fZFRb4IwFIX/StN3oIIb2AjGzZiZuEgE97C3WitgoGW9xfjzVxU3lyU+3vbce8797nhyamp0FBoqJWM8cAlGQnK1q2QR400+dyI8ScbAmtpv6bQzpVyLr06AQbZRAr3+xLjTkioGFVDJGgHUcJpN35fUdwlttTKKqxqjKYDQxlq9KgldI3Qm9LHiYrNexrg0pqWeVyvO6lKBoSMyIt7ZwMuyFUYz61pJZi5J/4kjEhEvs7FrkVWFXMl+NEZzpbm4RI/xntVgnxazGLMBI8WwfC59vtvZYnsQfsAPBSdhET5ZDaQMoDqK3y6ATiwkGCZNjH0yCB0SOP4wHwR0GNBg5IZB9IlR2u/7Uskrx0dwtlcR0Lc8T510leUYfdzuYQW4p08v7voe++PB7MYaJ2dYYGk13cECdKF1f7i50Ok9V1KcjCvrsXfvlfTl38Mn3w=="},
    new String[]{"Signature", "WWDMxCvr5erxB7H4U6BeI5e/l+EtQQUNPixjMLpfGHekfrQ86u7YEoJH8ZgN2bJG4qoWld6fjuh10j8qehp62Qdktxh81iZUySKkt8xGaWEuWMYzZDPdNsucn8HhSzcp0jyeT5ZihShMFO0Dv/KSCBpAt7WKrrZUrATF6cYoz7HsW3hJLh0lLP+Kr6MA6SrtrCxhaYbKhcZ+3wNHoOYQZhbqxIOvfuihRVnCxm8541gmjtHxj3x/qtQRqswOF7k24wUj6/H5Klj/A1/sZh1MZ5jDHfw795YIH3LqpFPzhWl5XEJ+K84vp0RRWaTUOb1hUPlLLWH8tH0Kd4RhaOcNpQ=="}
  );

  private List<String[]> formParams = Collections.singletonList(
    new String[]{"SAMLRequest", "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz48c2FtbDJwOkF1dGhuUmVxdWVzdCB4bWxuczpzYW1sMnA9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDpwcm90b2NvbCIgQXNzZXJ0aW9uQ29uc3VtZXJTZXJ2aWNlVVJMPSJodHRwOi8vbG9jYWxob3N0OjkwOTAvc2FtbC9TU08iIERlc3RpbmF0aW9uPSJodHRwOi8vbG9jYWxob3N0OjgwODAvU2luZ2xlU2lnbk9uU2VydmljZSIgRm9yY2VBdXRobj0iZmFsc2UiIElEPSJhMzQ4MWU3NGZkZWliYWQxNzlnNDZoZGQzaGEzMmIiIElzUGFzc2l2ZT0iZmFsc2UiIElzc3VlSW5zdGFudD0iMjAxNy0wMy0yOVQwNzoxNToxNC4zODBaIiBQcm90b2NvbEJpbmRpbmc9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDpiaW5kaW5nczpIVFRQLVBPU1QiIFZlcnNpb249IjIuMCI+PHNhbWwyOklzc3VlciB4bWxuczpzYW1sMj0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOmFzc2VydGlvbiI+aHR0cDovL21vY2stc3A8L3NhbWwyOklzc3Vlcj48ZHM6U2lnbmF0dXJlIHhtbG5zOmRzPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwLzA5L3htbGRzaWcjIj48ZHM6U2lnbmVkSW5mbz48ZHM6Q2Fub25pY2FsaXphdGlvbk1ldGhvZCBBbGdvcml0aG09Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvMTAveG1sLWV4Yy1jMTRuIyIvPjxkczpTaWduYXR1cmVNZXRob2QgQWxnb3JpdGhtPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwLzA5L3htbGRzaWcjcnNhLXNoYTEiLz48ZHM6UmVmZXJlbmNlIFVSST0iI2EzNDgxZTc0ZmRlaWJhZDE3OWc0NmhkZDNoYTMyYiI+PGRzOlRyYW5zZm9ybXM+PGRzOlRyYW5zZm9ybSBBbGdvcml0aG09Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvMDkveG1sZHNpZyNlbnZlbG9wZWQtc2lnbmF0dXJlIi8+PGRzOlRyYW5zZm9ybSBBbGdvcml0aG09Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvMTAveG1sLWV4Yy1jMTRuIyIvPjwvZHM6VHJhbnNmb3Jtcz48ZHM6RGlnZXN0TWV0aG9kIEFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvMjAwMC8wOS94bWxkc2lnI3NoYTEiLz48ZHM6RGlnZXN0VmFsdWU+RUcxaGFiN2tiN1BXNkNGSXpjZUUrdWptQWlNPTwvZHM6RGlnZXN0VmFsdWU+PC9kczpSZWZlcmVuY2U+PC9kczpTaWduZWRJbmZvPjxkczpTaWduYXR1cmVWYWx1ZT5CbU5FWlJFNHVoQWQ5dW1OL2psaEVTcXMvT3pOc3lienlPbXc5ZEF3aU52MTBXa2c3dXllcVcrTFpQenEwVUR6Y1dNNG4zakdsMmZRMllyRzFtdmg3Y05Ed3NLbEVrMEpLdnQ3cU9ad0Z5K1NPbGwza1BIY0x2Uit1NWxuaG5tQmRCOUwxWHBhNFFHNjQzR01PanpIUHkwRDVDVUlwUzMwRzVkeVBwNGdXZHpSK2Z4YmtQcXZYMTc4bVJTcWF2U3NTOXRBMVhXS2s3eTBWNHovbkdnV3hBVDJGdWUrbXNlSXVLc2VMcXFzWmNCVEpVYThUS3FxUGdWUmpEMTlNV2syTEE0WTdMSGI3VzFuU1IwSXJ4ZGdJVlJhbGEwNWtUN3NndG91TW91MGZVc1hlYTFIMXF5WHRETlh5N0ovZENTYUVTdTlQNWhTYlRsSitjNWFsbFJnbnc9PTwvZHM6U2lnbmF0dXJlVmFsdWU+PGRzOktleUluZm8+PGRzOlg1MDlEYXRhPjxkczpYNTA5Q2VydGlmaWNhdGU+TUlJREV6Q0NBZnVnQXdJQkFnSUpBS29LL2hlQmpjT1lNQTBHQ1NxR1NJYjNEUUVCQlFVQU1DQXhIakFjQmdOVkJBb01GVTl5WjJGdQphWHBoZEdsdmJpd2dRMDQ5VDBsRVF6QWVGdzB4TlRFeE1URXhNREV5TVRWYUZ3MHlOVEV4TVRBeE1ERXlNVFZhTUNBeEhqQWNCZ05WCkJBb01GVTl5WjJGdWFYcGhkR2x2Yml3Z1EwNDlUMGxFUXpDQ0FTSXdEUVlKS29aSWh2Y05BUUVCQlFBRGdnRVBBRENDQVFvQ2dnRUIKQU5CR3dKL3FwVFFOaVNnVWdsU0UyVXpFa1Vvdyt3UzhyNjdldHhvRWhsekpaZmdLL2s1VGZHMXdJQ0RxYXBIQXhFVmdVTTEwYUJIUgpjdE5vY0E1d21sSHR4ZGlkaHpSWnJvcUh3cEt5MkJtc0tYNVoyb0syNVJMcHN5dXNCMUtyb2VtZ0EvQ2pVbkk2cklMMXh4Rm4zS3lPCkZoMVpCTFVRdEtOUWVNUzdIRkdnU0RBcCtzWHVURnVqejEyTEZEdWdYMFQwS0I1YTErMGw4eTBQRWEweUdhMW9pNnNlT054ODQ5WkgKeE0wUFJ2VXVuV2t1VE0rZm9aMGpacEZhcFhlMDJ5V01xaGMvMmlZTWllRS8zR3ZPZ3VKY2hKdDZSK2N1dDhWQmI2dWJLVUlHSzdwbQpvcS9UQjZEVlhwdnNIcXNESlhlY2h4Y2ljdTRwZEtWREhTZWM4NTBDQXdFQUFhTlFNRTR3SFFZRFZSME9CQllFRks3UnFqb29kU1lWClhHVFZFZExmM2tKZmxQL3NNQjhHQTFVZEl3UVlNQmFBRks3UnFqb29kU1lWWEdUVkVkTGYza0pmbFAvc01Bd0dBMVVkRXdRRk1BTUIKQWY4d0RRWUpLb1pJaHZjTkFRRUZCUUFEZ2dFQkFETlpreGxGWGg0RjQ1bXVDYm5RZCtXbWFYbEd2Yjl0a1V5QUl4Vkw4QUl1OEoxOApGNDIwdnBuR3BvVUFFK0h5M2V2Qm1wMm5rckZBZ21yMDU1ZkFqcEhlWkZnRFpCQVBDd1lkM1ROTURlU3lNdGEzS2Erb1M3R1JGRGVQCmtNRW0ra0g0L3JJVE5LVUYxc092V0JUU293azlUdWRFRHlGcWdHbnRjZHUvbC96Unh2eDMzeTNMTUc1VVNEMHg0WDRJS2pSclJOMUIKYmNLZ2k4ZHExMEMzamRxTmFuY1R1UG9xVDNXV3pSdlZ0Qi9xMzRCN0Y3NC82SnpnRW9PQ0VIdWZCTXA0WkZ1NTRQMHlFR3RXZlR3VAp6dW9ab2JyQ2hWVkJ0NHcvWFphZ3JSdFVDRE53UnBITmJwanhZdWRicUxxcGkxTVFwVjlvaHQvQnBUSFZKRzJpMHJvPTwvZHM6WDUwOUNlcnRpZmljYXRlPjwvZHM6WDUwOURhdGE+PC9kczpLZXlJbmZvPjwvZHM6U2lnbmF0dXJlPjwvc2FtbDJwOkF1dGhuUmVxdWVzdD4="}
  );

  @Test
  public void singleSignOnServiceGet() throws Exception {
    doSingleSignOn(false);
  }

  @Test
  public void singleSignOnServicePost() throws Exception {
    doSingleSignOn(true);
  }

  private void doSingleSignOn(boolean post) throws Exception {
    CookieFilter cookieFilter = login("admin", "secret", SC_MOVED_TEMPORARILY);
    RequestSpecification requestSpecification = given();
    if (post) {
      formParams.forEach(param -> requestSpecification.formParam(param[0], param[1]));
    } else {
      params.forEach(param -> requestSpecification.param(param[0], param[1]));
    }
    requestSpecification.filter(cookieFilter);

    String path = "/SingleSignOnService";
    Response response = post ? requestSpecification.post(path) : requestSpecification.get(path);

    response
      .then()
      .statusCode(SC_OK);

    String html = response.getBody().asString();

    Matcher matcher = Pattern.compile("name=\"SAMLResponse\" value=\"(.*?)\"").matcher(html);
    matcher.find();
    String samlResponse = new String(Base64.getDecoder().decode(matcher.group(1)));
    assertThat(String.format("SAML response should contain the '%s' attribute", "attribute 1"), samlResponse.contains("attribute 1"));
    assertThat(String.format("SAML response should contain the '%s' attribute value", "admin attribute 1"), samlResponse.contains("admin attribute"));
    assertThat(String.format("SAML response should not contain the '%s' attribute", "test-attribute"), !samlResponse.contains("test-attribute"));
  }
}
