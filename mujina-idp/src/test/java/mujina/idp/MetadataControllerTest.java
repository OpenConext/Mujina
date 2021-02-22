package mujina.idp;

import mujina.AbstractIntegrationTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;

import static io.restassured.RestAssured.given;
import static io.restassured.config.RestAssuredConfig.newConfig;
import static io.restassured.config.XmlConfig.xmlConfig;
import static org.apache.http.HttpStatus.SC_OK;
import static org.hamcrest.Matchers.equalTo;

public class MetadataControllerTest extends AbstractIntegrationTest {

  @Value("${idp.base_url}")
  private String idpBaseUrl;

  @Test
  public void metadata() throws Exception {
    given()
      .config(newConfig()
        .xmlConfig(xmlConfig().declareNamespace("md", "urn:oasis:names:tc:SAML:2.0:metadata")))
      .get("/metadata")
      .then()
      .contentType("application/xml")
      .statusCode(SC_OK)
      .body(
        "EntityDescriptor.IDPSSODescriptor.SingleSignOnService.@Location",
        equalTo(idpBaseUrl + "/SingleSignOnService"))
      .body(
        "EntityDescriptor.IDPSSODescriptor.SingleLogoutService.@Location",
        equalTo(idpBaseUrl + "/SingleLogoutService"));
  }

}


