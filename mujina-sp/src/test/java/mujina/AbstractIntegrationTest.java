package mujina;

import io.restassured.RestAssured;
import io.restassured.filter.cookie.CookieFilter;
import mujina.api.SpConfiguration;
import mujina.saml.KeyStoreLocator;
import net.shibboleth.shared.resolver.CriteriaSet;
import net.shibboleth.shared.xml.SerializeSupport;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.opensaml.core.config.InitializationService;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.schema.XSString;
import org.opensaml.core.xml.schema.impl.XSStringBuilder;
import org.opensaml.core.xml.util.XMLObjectSupport;
import org.opensaml.saml.common.SAMLVersion;
import org.opensaml.saml.saml2.core.*;
import org.opensaml.security.credential.Credential;
import org.opensaml.security.credential.UsageType;
import org.opensaml.security.credential.impl.KeyStoreCredentialResolver;
import org.opensaml.security.criteria.UsageCriterion;
import org.opensaml.xmlsec.signature.Signature;
import org.opensaml.xmlsec.signature.support.SignatureConstants;
import org.opensaml.xmlsec.signature.support.Signer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringRunner;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import java.security.KeyStore;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.restassured.RestAssured.given;
import static org.apache.http.HttpStatus.SC_MOVED_TEMPORARILY;
import static org.apache.http.HttpStatus.SC_OK;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class AbstractIntegrationTest {

    @Autowired
    protected SpConfiguration spConfiguration;

    @LocalServerPort
    protected int serverPort;

    @Value("${sp.entity_id}")
    private String spEntityId;

    @Value("${sp.private_key}")
    private String spPrivateKey;

    @Value("${sp.certificate}")
    private String spCertificate;

    @Value("${sp.passphrase}")
    private String spPassphrase;

    @Value("${sp.base_url}")
    private String spBaseUrl;

    @Value("${sp.acs_location_path}")
    private String acsLocationPath;

    private Credential idpSigningCredential;

    @Before
    public void before() throws Exception {
        InitializationService.initialize();

        RestAssured.port = serverPort;
        given()
                .header("Content-Type", "application/json")
                .post("/api/reset")
                .then()
                .statusCode(SC_OK);

        //The demo IdP metadata's embedded certificate is the same demo keypair as sp.private_key/sp.certificate.
        KeyStore keyStore = KeyStoreLocator.createKeyStore(spPassphrase);
        KeyStoreLocator.addPrivateKey(keyStore, spEntityId, spPrivateKey, spCertificate, spPassphrase);
        KeyStoreCredentialResolver resolver = new KeyStoreCredentialResolver(
                keyStore, Map.of(spEntityId, spPassphrase), UsageType.SIGNING);
        idpSigningCredential = resolver.resolveSingle(new CriteriaSet(new EntityIdCriterion(spEntityId), new UsageCriterion(UsageType.SIGNING)));
    }

    protected CookieFilter login() throws Exception {
        CookieFilter cookieFilter = new CookieFilter();

        String html = given()
                .filter(cookieFilter)
                .get("/saml2/authenticate?registrationId=idp")
                .getBody().asString();

        Matcher matcher = Pattern.compile("name=\"SAMLRequest\"[^>]*value=\"(.*?)\"").matcher(html);
        matcher.find();
        String samlRequest = new String(Base64.getDecoder().decode(matcher.group(1)));

        //Now mimic a response message
        String samlResponse = getIdPSAMLResponse(samlRequest);
        given()
                .formParam("SAMLResponse", Base64.getEncoder().encodeToString(samlResponse.getBytes()))
                .filter(cookieFilter)
                .post(acsLocationPath)
                .then()
                .statusCode(SC_MOVED_TEMPORARILY);

        return cookieFilter;
    }

    private String getIdPSAMLResponse(String authnRequestXml) throws Exception {
        Matcher matcher = Pattern.compile("ID=\"(.*?)\"").matcher(authnRequestXml);
        matcher.find();
        //We need the ID of the original request to mimic the real IdP authnResponse
        String inResponseTo = matcher.group(1);

        String acsUrl = spBaseUrl + acsLocationPath;
        Instant now = Instant.now();

        Response response = buildSAMLObject(Response.class);
        response.setDestination(acsUrl);
        response.setID(randomId());
        response.setInResponseTo(inResponseTo);
        response.setIssueInstant(now);
        response.setVersion(SAMLVersion.VERSION_20);
        response.setIssuer(issuer());

        Status status = buildSAMLObject(Status.class);
        StatusCode statusCode = buildSAMLObject(StatusCode.class);
        statusCode.setValue(StatusCode.SUCCESS);
        status.setStatusCode(statusCode);
        response.setStatus(status);

        Assertion assertion = buildSAMLObject(Assertion.class);
        assertion.setID(randomId());
        assertion.setIssueInstant(now);
        assertion.setVersion(SAMLVersion.VERSION_20);
        assertion.setIssuer(issuer());

        Subject subject = buildSAMLObject(Subject.class);
        NameID nameID = buildSAMLObject(NameID.class);
        nameID.setValue("admin");
        nameID.setFormat(NameIDType.UNSPECIFIED);
        subject.setNameID(nameID);

        SubjectConfirmation confirmation = buildSAMLObject(SubjectConfirmation.class);
        confirmation.setMethod(SubjectConfirmation.METHOD_BEARER);
        SubjectConfirmationData confirmationData = buildSAMLObject(SubjectConfirmationData.class);
        confirmationData.setInResponseTo(inResponseTo);
        confirmationData.setNotBefore(now.minus(Duration.ofMinutes(5)));
        confirmationData.setNotOnOrAfter(now.plus(Duration.ofMinutes(10)));
        confirmationData.setRecipient(acsUrl);
        confirmation.setSubjectConfirmationData(confirmationData);
        subject.getSubjectConfirmations().add(confirmation);
        assertion.setSubject(subject);

        Conditions conditions = buildSAMLObject(Conditions.class);
        conditions.setNotBefore(now.minus(Duration.ofMinutes(5)));
        conditions.setNotOnOrAfter(now.plus(Duration.ofMinutes(10)));
        AudienceRestriction audienceRestriction = buildSAMLObject(AudienceRestriction.class);
        Audience audience = buildSAMLObject(Audience.class);
        audience.setURI(spEntityId);
        audienceRestriction.getAudiences().add(audience);
        conditions.getAudienceRestrictions().add(audienceRestriction);
        assertion.setConditions(conditions);

        AuthnStatement authnStatement = buildSAMLObject(AuthnStatement.class);
        authnStatement.setAuthnInstant(now);
        AuthnContext authnContext = buildSAMLObject(AuthnContext.class);
        AuthnContextClassRef authnContextClassRef = buildSAMLObject(AuthnContextClassRef.class);
        authnContextClassRef.setURI(AuthnContext.PASSWORD_AUTHN_CTX);
        authnContext.setAuthnContextClassRef(authnContextClassRef);
        authnStatement.setAuthnContext(authnContext);
        assertion.getAuthnStatements().add(authnStatement);

        Map<String, String> attributes = new LinkedHashMap<>();
        attributes.put("urn:mace:dir:attribute-def:cn", "John Doe");
        attributes.put("urn:mace:dir:attribute-def:displayName", "John Doe");
        attributes.put("urn:mace:dir:attribute-def:eduPersonPrincipalName", "j.doe@example.com");
        attributes.put("urn:mace:dir:attribute-def:givenName", "John");
        attributes.put("urn:mace:dir:attribute-def:mail", "j.doe@example.com");
        attributes.put("urn:mace:dir:attribute-def:sn", "Doe");
        attributes.put("urn:mace:dir:attribute-def:uid", "john.doe");
        attributes.put("urn:mace:terena.org:attribute-def:schacHomeOrganization", "example.com");

        AttributeStatement attributeStatement = buildSAMLObject(AttributeStatement.class);
        attributes.forEach((name, value) -> attributeStatement.getAttributes().add(buildAttribute(name, value)));
        assertion.getAttributeStatements().add(attributeStatement);

        signAssertion(assertion);
        response.getAssertions().add(assertion);

        Element element = XMLObjectSupport.marshall(response);
        return SerializeSupport.nodeToString(element);
    }

    private Issuer issuer() {
        Issuer issuer = buildSAMLObject(Issuer.class);
        issuer.setValue("http://mock-idp");
        issuer.setFormat(NameIDType.ENTITY);
        return issuer;
    }

    private void signAssertion(Assertion assertion) throws Exception {
        Signature signature = buildSAMLObject(Signature.class);
        signature.setSigningCredential(idpSigningCredential);
        signature.setSignatureAlgorithm(SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA256);
        signature.setCanonicalizationAlgorithm(SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);
        assertion.setSignature(signature);

        XMLObjectProviderRegistrySupport.getMarshallerFactory().getMarshaller(assertion).marshall(assertion);
        Signer.signObject(signature);
    }

    private Attribute buildAttribute(String name, String value) {
        XSStringBuilder stringBuilder = (XSStringBuilder) XMLObjectProviderRegistrySupport.getBuilderFactory().getBuilder(XSString.TYPE_NAME);
        XSString stringValue = stringBuilder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME, XSString.TYPE_NAME);
        stringValue.setValue(value);

        Attribute attribute = buildSAMLObject(Attribute.class);
        attribute.setName(name);
        attribute.setNameFormat(Attribute.URI_REFERENCE);
        attribute.getAttributeValues().add(stringValue);
        return attribute;
    }

    private static String randomId() {
        return "_" + UUID.randomUUID();
    }

    @SuppressWarnings("unchecked")
    private static <T extends XMLObject> T buildSAMLObject(final Class<T> objectClass) {
        try {
            QName qName = (QName) objectClass.getDeclaredField("DEFAULT_ELEMENT_NAME").get(null);
            return (T) XMLObjectProviderRegistrySupport.getBuilderFactory().getBuilder(qName).buildObject(qName);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

}
