package mujina.api;

import mujina.AbstractIntegrationTest;
import org.junit.Test;

import static io.restassured.RestAssured.given;
import static org.apache.http.HttpStatus.SC_OK;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;

public class SpControllerTest extends AbstractIntegrationTest {

  @Test
  public void setSsoServiceURL() throws Exception {
    String acs = "https://localhost:8080/ssoServiceURL";
    api(acs, "/api/ssoServiceURL");

    assertEquals(acs, spConfiguration.getIdpSSOServiceURL());
  }

  @Test
  public void setProtocolBinding() throws Exception {
    String protocolBinding = "urn:oasis:names:tc:SAML:2.0:bindings:HTTP-REDIRECT";
    api(protocolBinding, "/api/protocolBinding");

    assertEquals(protocolBinding, spConfiguration.getProtocolBinding());
  }

  @Test
  public void setAssertionConsumerServiceURL() throws Exception {
    String assertionConsumerServiceURL = "https://localhost:8080/assertionConsumerServiceURL";
    api(assertionConsumerServiceURL, "/api/assertionConsumerServiceURL");

    assertEquals(assertionConsumerServiceURL, spConfiguration.getAssertionConsumerServiceURL());
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
