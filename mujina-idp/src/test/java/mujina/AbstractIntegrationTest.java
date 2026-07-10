package mujina;

import io.restassured.RestAssured;
import io.restassured.filter.cookie.CookieFilter;
import mujina.api.IdpConfiguration;
import mujina.saml.KeyStoreLocator;
import mujina.saml.SAMLBuilder;
import org.opensaml.Configuration;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.xml.io.Unmarshaller;
import org.opensaml.xml.parse.BasicParserPool;
import org.opensaml.xml.security.CriteriaSet;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.security.criteria.EntityIDCriteria;
import org.opensaml.xml.signature.SignatureConstants;
import org.opensaml.xml.util.XMLHelper;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.security.saml.key.JKSKeyManager;
import org.springframework.test.context.junit4.SpringRunner;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.Signature;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.zip.Deflater;

import static io.restassured.RestAssured.given;
import static org.apache.http.HttpStatus.SC_OK;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {"idp.auth_method=USER"})
public abstract class AbstractIntegrationTest {

    @Autowired
    protected IdpConfiguration idpConfiguration;

    @LocalServerPort
    protected int serverPort;

    @Value("${idp.entity_id}")
    private String idpEntityId;

    @Value("${idp.private_key}")
    private String idpPrivateKey;

    @Value("${idp.certificate}")
    private String idpCertificate;

    @Value("${idp.passphrase}")
    private String idpPassphrase;

    private Credential signingCredential;

    @Before
    public void before() throws Exception {
        RestAssured.port = serverPort;
        given()
                .header("Content-Type", "application/json")
                .post("/api/reset")
                .then()
                .statusCode(SC_OK);

        KeyStore keyStore = KeyStoreLocator.createKeyStore(idpPassphrase);
        KeyStoreLocator.addPrivateKey(keyStore, idpEntityId, idpPrivateKey, idpCertificate, idpPassphrase);
        JKSKeyManager keyManager = new JKSKeyManager(keyStore, Collections.singletonMap(idpEntityId, idpPassphrase), idpEntityId);
        signingCredential = keyManager.resolveSingle(new CriteriaSet(new EntityIDCriteria(idpEntityId)));
    }

    protected CookieFilter login(String username, String password, int statusCode) {
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

    // HTTP-Redirect binding: deflate + base64 the AuthnRequest, then sign the query string
    // (SAMLRequest + SigAlg) with the SP's private key, per SAML core 3.4.4.1.
    protected List<String[]> redirectBindingParams(boolean forceAuthn) throws Exception {
        AuthnRequest authnRequest = loadAuthnRequest();
        authnRequest.setForceAuthn(forceAuthn);
        Configuration.getMarshallerFactory().getMarshaller(authnRequest).marshall(authnRequest);

        String xml = XMLHelper.nodeToString(authnRequest.getDOM());
        String samlRequest = Base64.getEncoder().encodeToString(deflate(xml));
        String sigAlg = SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA256;

        String signingInput = "SAMLRequest=" + urlEncode(samlRequest) + "&SigAlg=" + urlEncode(sigAlg);
        Signature signer = Signature.getInstance("SHA256withRSA");
        signer.initSign(signingCredential.getPrivateKey());
        signer.update(signingInput.getBytes(StandardCharsets.UTF_8));
        String signatureValue = Base64.getEncoder().encodeToString(signer.sign());

        return Arrays.asList(
                new String[]{"SAMLRequest", samlRequest},
                new String[]{"SigAlg", sigAlg},
                new String[]{"Signature", signatureValue});
    }

    // HTTP-POST binding: sign the AuthnRequest XML itself (embedded <ds:Signature>), then base64 it.
    protected List<String[]> postBindingParams(boolean forceAuthn) throws Exception {
        AuthnRequest authnRequest = loadAuthnRequest();
        authnRequest.setForceAuthn(forceAuthn);
        SAMLBuilder.signAssertion(authnRequest, signingCredential);

        String xml = XMLHelper.nodeToString(authnRequest.getDOM());
        String samlRequest = Base64.getEncoder().encodeToString(xml.getBytes(StandardCharsets.UTF_8));

        return Collections.singletonList(new String[]{"SAMLRequest", samlRequest});
    }

    private AuthnRequest loadAuthnRequest() throws Exception {
        BasicParserPool parserPool = new BasicParserPool();
        parserPool.setNamespaceAware(true);
        try (InputStream inputStream = getClass().getResourceAsStream("/saml/authn-request.xml")) {
            Document document = parserPool.parse(inputStream);
            Element element = document.getDocumentElement();
            Unmarshaller unmarshaller = Configuration.getUnmarshallerFactory().getUnmarshaller(element);
            return (AuthnRequest) unmarshaller.unmarshall(element);
        }
    }

    private static byte[] deflate(String input) {
        Deflater deflater = new Deflater(Deflater.DEFLATED, true);
        deflater.setInput(input.getBytes(StandardCharsets.UTF_8));
        deflater.finish();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        while (!deflater.finished()) {
            int count = deflater.deflate(buffer);
            outputStream.write(buffer, 0, count);
        }
        deflater.end();
        return outputStream.toByteArray();
    }

    private static String urlEncode(String value) throws Exception {
        return URLEncoder.encode(value, StandardCharsets.UTF_8.name());
    }

}
