package mujina.sp;

import mujina.AbstractIntegrationTest;
import org.junit.Test;

import static io.restassured.RestAssured.given;
import static io.restassured.config.RestAssuredConfig.newConfig;
import static io.restassured.config.XmlConfig.xmlConfig;
import static org.apache.http.HttpStatus.SC_OK;
import static org.hamcrest.Matchers.equalTo;

public class MetadataEndPointTest extends AbstractIntegrationTest {

    @Test
    public void metadata() throws Exception {
        given()
                .config(newConfig()
                        .xmlConfig(xmlConfig().declareNamespace("md", "urn:oasis:names:tc:SAML:2.0:metadata")))
                .header("Content-Type", "application/xml")
                .get("/metadata")
                .then()
                .statusCode(SC_OK)
                .body(
                        "EntityDescriptor.SPSSODescriptor.AssertionConsumerService.@Location",
                        equalTo("http://localhost:9090/saml/SSO"));
    }

    // Regression test for https://github.com/OpenConext/Mujina/issues/99 -
    // changing the entityId via the API left the signing credential's keystore alias
    // out of sync, so /metadata crashed trying to sign the EntityDescriptor.
    @Test
    public void metadataAfterEntityIdChange() throws Exception {
        String newEntityId = "http://new-sp-entity-id";
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


