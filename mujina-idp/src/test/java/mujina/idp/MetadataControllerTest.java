package mujina.idp;

import io.restassured.response.Response;
import mujina.AbstractIntegrationTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;

import java.time.Duration;
import java.time.Instant;

import static io.restassured.RestAssured.given;
import static io.restassured.config.RestAssuredConfig.newConfig;
import static io.restassured.config.XmlConfig.xmlConfig;
import static org.apache.http.HttpStatus.SC_OK;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertTrue;

public class MetadataControllerTest extends AbstractIntegrationTest {

    @Value("${idp.base_url}")
    private String idpBaseUrl;

    @Test
    public void metadata() {
        Instant before = Instant.now();

        Response response = given()
                .config(newConfig()
                        .xmlConfig(xmlConfig().declareNamespace("md", "urn:oasis:names:tc:SAML:2.0:metadata")))
                .header("Content-Type", "application/xml")
                .get("/metadata");

        response.then()
                .statusCode(SC_OK)
                .body(
                        "EntityDescriptor.IDPSSODescriptor.SingleSignOnService.@Location",
                        equalTo(idpBaseUrl + "/SingleSignOnService"))
                .body("EntityDescriptor.IDPSSODescriptor.SingleSignOnService.@Binding",
                        equalTo("urn:oasis:names:tc:SAML:2.0:bindings:HTTP-Redirect"));

        String validUntil = response.xmlPath().getString("EntityDescriptor.@validUntil");
        Instant actual = Instant.parse(validUntil);
        Instant expected = before.plusMillis(86400000);
        assertTrue("validUntil should be ~24h from now, was " + actual,
                Duration.between(expected, actual).abs().toSeconds() < 10);
    }

    // Regression test for https://github.com/OpenConext/Mujina/issues/99 -
    // changing the entityId via the API left the signing credential's keystore alias
    // out of sync, so /metadata crashed trying to sign the EntityDescriptor.
    @Test
    public void metadataAfterEntityIdChange() {
        String newEntityId = "http://new-idp-entity-id";
        given()
                .body(newEntityId)
                .header("Content-Type", "application/json")
                .put("/api/entityid")
                .then()
                .statusCode(SC_OK);

        given()
                .config(newConfig()
                        .xmlConfig(xmlConfig().declareNamespace("md", "urn:oasis:names:tc:SAML:2.0:metadata")))
                .header("Content-Type", "application/xml")
                .get("/metadata")
                .then()
                .statusCode(SC_OK)
                .body("EntityDescriptor.@entityID", equalTo(newEntityId));
    }

}
