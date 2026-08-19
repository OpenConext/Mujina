package mujina.idp;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mujina.api.IdpConfiguration;
import mujina.idp.saml.SAMLBuilder;
import mujina.idp.saml.SAMLPrincipal;
import net.shibboleth.shared.resolver.CriteriaSet;
import net.shibboleth.shared.xml.SerializeSupport;
import net.shibboleth.shared.xml.impl.BasicParserPool;
import org.apache.commons.text.StringEscapeUtils;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.util.XMLObjectSupport;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.Status;
import org.opensaml.saml.saml2.core.StatusCode;
import org.opensaml.security.credential.Credential;
import org.opensaml.security.credential.UsageType;
import org.opensaml.security.credential.impl.KeyStoreCredentialResolver;
import org.opensaml.security.criteria.UsageCriterion;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import saml.parser.EncodingUtils;
import saml.parser.OpenSamlVelocityEngine;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;
import static mujina.idp.saml.SAMLBuilder.buildAssertion;
import static mujina.idp.saml.SAMLBuilder.buildIssuer;
import static mujina.idp.saml.SAMLBuilder.buildSAMLObject;
import static mujina.idp.saml.SAMLBuilder.buildStatus;
import static mujina.idp.saml.SAMLBuilder.randomSAMLId;
import static mujina.idp.saml.SAMLBuilder.signAssertion;

public class SAMLMessageHandler {

    private final OpenSamlVelocityEngine velocityEngine = new OpenSamlVelocityEngine();
    private final BasicParserPool parserPool;
    private final IdpConfiguration idpConfiguration;
    private final int clockSkew;
    private final int expires;
    private final boolean compareEndpoints;
    private final String idpBaseUrl;

    public SAMLMessageHandler(BasicParserPool parserPool,
                              IdpConfiguration idpConfiguration,
                              int clockSkew,
                              int expires,
                              boolean compareEndpoints,
                              String idpBaseUrl) {
        this.parserPool = parserPool;
        this.idpConfiguration = idpConfiguration;
        this.clockSkew = clockSkew;
        this.expires = expires;
        this.compareEndpoints = compareEndpoints;
        this.idpBaseUrl = idpBaseUrl;
    }

    public AuthnRequest parseAuthnRequest(HttpServletRequest request, boolean postRequest) {
        try {
            String samlRequestParam = request.getParameter("SAMLRequest");
            String xml = EncodingUtils.samlDecode(samlRequestParam, !postRequest);

            Document document = parserPool.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
            Element element = document.getDocumentElement();
            AuthnRequest authnRequest = (AuthnRequest) XMLObjectProviderRegistrySupport
                    .getUnmarshallerFactory().getUnmarshaller(element).unmarshall(element);

            Instant now = Instant.now();
            Instant issueInstant = authnRequest.getIssueInstant();
            if (issueInstant == null
                    || issueInstant.isBefore(now.minusSeconds(clockSkew))
                    || issueInstant.isAfter(now.plusSeconds(clockSkew + expires))) {
                throw new IllegalArgumentException("AuthnRequest issue instant out of tolerance: " + issueInstant);
            }

            if (compareEndpoints) {
                String destination = authnRequest.getDestination();
                String expectedDestination = idpBaseUrl + "/SingleSignOnService";
                if (destination != null && !destination.equalsIgnoreCase(expectedDestination)) {
                    throw new IllegalArgumentException(
                            String.format("Destination %s does not match configured base URL %s", destination, expectedDestination));
                }
            }

            return authnRequest;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void sendAuthnResponse(SAMLPrincipal principal,
                                  String authnContextClassRefValue,
                                  HttpServletResponse response) throws Exception {
        Status status = buildStatus(StatusCode.SUCCESS);

        String entityId = idpConfiguration.getEntityId();
        Credential signingCredential = resolveCredential(entityId);

        Response authResponse = buildSAMLObject(Response.class);
        Issuer issuer = buildIssuer(entityId);

        authResponse.setIssuer(issuer);
        authResponse.setID(randomSAMLId());
        authResponse.setIssueInstant(Instant.now());
        authResponse.setInResponseTo(principal.getRequestID());

        Assertion assertion = buildAssertion(principal, authnContextClassRefValue, status, entityId);
        signAssertion(assertion, signingCredential, idpConfiguration.getSignatureAlgorithm());

        authResponse.getAssertions().add(assertion);
        authResponse.setDestination(principal.getAssertionConsumerServiceURL());
        authResponse.setStatus(status);

        Element element = XMLObjectSupport.marshall(authResponse);
        String samlResponse = SerializeSupport.nodeToString(element);

        Map<String, Object> model = new HashMap<>();
        model.put("action", principal.getAssertionConsumerServiceURL());
        model.put("SAMLResponse", EncodingUtils.samlEncode(samlResponse));
        if (StringUtils.hasText(principal.getRelayState())) {
            model.put("RelayState", EncodingUtils.toISO8859_1(StringEscapeUtils.escapeHtml4(principal.getRelayState())));
        }

        response.setContentType("text/html");
        response.setCharacterEncoding(UTF_8.name());
        response.setHeader("Cache-Control", "no-cache, no-store, max-age=0, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");

        StringWriter out = new StringWriter();
        velocityEngine.process(model, out);
        response.getWriter().write(out.toString());
    }

    private Credential resolveCredential(String entityId) {
        try {
            KeyStoreCredentialResolver resolver = new KeyStoreCredentialResolver(
                    idpConfiguration.getKeyStore(),
                    Map.of(entityId, idpConfiguration.getKeystorePassword()),
                    UsageType.SIGNING);
            return resolver.resolveSingle(new CriteriaSet(new EntityIdCriterion(entityId), new UsageCriterion(UsageType.SIGNING)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
