package mujina.idp;

import mujina.AbstractIntegrationTest;
import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.joda.time.DateTimeZone;
import org.junit.After;
import org.junit.Before;
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
    private long fixedSystemTime;

    @Before
    public void setUp() {
        fixedSystemTime = System.currentTimeMillis();
        DateTimeUtils.setCurrentMillisFixed(fixedSystemTime);
    }

    @After
    public void tearDown() {
        DateTimeUtils.setCurrentMillisSystem();
    }

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
                        "EntityDescriptor.IDPSSODescriptor.SingleSignOnService.@Location",
                        equalTo(idpBaseUrl + "/SingleSignOnService"))
                .body("EntityDescriptor.IDPSSODescriptor.SingleSignOnService.@Binding",
                    equalTo("urn:oasis:names:tc:SAML:2.0:bindings:HTTP-Redirect"))
                .body("EntityDescriptor.@validUntil", equalTo(
                    new DateTime().withZone(DateTimeZone.UTC).plusMillis(86400000)
                        .toString("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")));
    }

}


