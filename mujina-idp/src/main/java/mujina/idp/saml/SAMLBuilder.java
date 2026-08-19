package mujina.idp.saml;

import mujina.saml.SAMLAttribute;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.core.xml.schema.XSString;
import org.opensaml.core.xml.schema.impl.XSStringBuilder;
import org.opensaml.saml.saml2.core.*;
import org.opensaml.security.credential.Credential;
import org.opensaml.xmlsec.signature.Signature;
import org.opensaml.xmlsec.signature.SignableXMLObject;
import org.opensaml.xmlsec.signature.support.SignatureConstants;
import org.opensaml.xmlsec.signature.support.SignatureException;
import org.opensaml.xmlsec.signature.support.Signer;
import org.springframework.util.StringUtils;

import javax.xml.namespace.QName;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static java.util.stream.Collectors.toList;

public class SAMLBuilder {

    @SuppressWarnings("unchecked")
    public static <T extends XMLObject> T buildSAMLObject(final Class<T> objectClass) {
        try {
            QName qName = (QName) objectClass.getDeclaredField("DEFAULT_ELEMENT_NAME").get(null);
            return (T) XMLObjectProviderRegistrySupport.getBuilderFactory().getBuilder(qName).buildObject(qName);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static Issuer buildIssuer(String issuingEntityName) {
        Issuer issuer = buildSAMLObject(Issuer.class);
        issuer.setValue(issuingEntityName);
        issuer.setFormat(NameIDType.ENTITY);
        return issuer;
    }

    private static Subject buildSubject(String subjectNameId, String subjectNameIdType, String recipient, String inResponseTo) {
        NameID nameID = buildSAMLObject(NameID.class);
        nameID.setValue(subjectNameId);
        nameID.setFormat(subjectNameIdType);

        Subject subject = buildSAMLObject(Subject.class);
        subject.setNameID(nameID);

        SubjectConfirmation subjectConfirmation = buildSAMLObject(SubjectConfirmation.class);
        subjectConfirmation.setMethod(SubjectConfirmation.METHOD_BEARER);

        SubjectConfirmationData subjectConfirmationData = buildSAMLObject(SubjectConfirmationData.class);

        subjectConfirmationData.setRecipient(recipient);
        subjectConfirmationData.setInResponseTo(inResponseTo);
        subjectConfirmationData.setNotOnOrAfter(Instant.now().plus(Duration.ofMinutes(8 * 60)));

        subjectConfirmation.setSubjectConfirmationData(subjectConfirmationData);

        subject.getSubjectConfirmations().add(subjectConfirmation);

        return subject;
    }

    public static Status buildStatus(String value) {
        Status status = buildSAMLObject(Status.class);
        StatusCode statusCode = buildSAMLObject(StatusCode.class);
        statusCode.setValue(value);
        status.setStatusCode(statusCode);
        return status;
    }

    public static Status buildStatus(String value, String subStatus, String message) {
        Status status = buildStatus(value);

        StatusCode subStatusCode = buildSAMLObject(StatusCode.class);
        subStatusCode.setValue(subStatus);
        status.getStatusCode().setStatusCode(subStatusCode);

        StatusMessage statusMessage = buildSAMLObject(StatusMessage.class);
        statusMessage.setValue(message);
        status.setStatusMessage(statusMessage);

        return status;
    }

    public static Assertion buildAssertion(SAMLPrincipal principal, String authnContextClassRefValue, Status status, String entityId) {
        Assertion assertion = buildSAMLObject(Assertion.class);

        if (status.getStatusCode().getValue().equals(StatusCode.SUCCESS)) {
            Subject subject = buildSubject(principal.getNameID(), principal.getNameIDType(), principal.getAssertionConsumerServiceURL(), principal.getRequestID());
            assertion.setSubject(subject);
        }

        Issuer issuer = buildIssuer(entityId);

        Audience audience = buildSAMLObject(Audience.class);
        audience.setURI(principal.getServiceProviderEntityID());
        AudienceRestriction audienceRestriction = buildSAMLObject(AudienceRestriction.class);
        audienceRestriction.getAudiences().add(audience);

        Instant now = Instant.now();
        Conditions conditions = buildSAMLObject(Conditions.class);
        conditions.setNotBefore(now.minus(Duration.ofMinutes(3)));
        conditions.setNotOnOrAfter(now.plus(Duration.ofMinutes(3)));
        conditions.getAudienceRestrictions().add(audienceRestriction);
        assertion.setConditions(conditions);

        AuthnStatement authnStatement = buildAuthnStatement(now, entityId, authnContextClassRefValue);

        assertion.setIssuer(issuer);
        assertion.getAuthnStatements().add(authnStatement);

        assertion.getAttributeStatements().add(buildAttributeStatement(principal.getAttributes()));

        assertion.setID(randomSAMLId());
        assertion.setIssueInstant(now);

        return assertion;
    }

    public static void signAssertion(SignableXMLObject signableXMLObject, Credential signingCredential, String signatureAlgorithm) throws MarshallingException, SignatureException {
        Signature signature = buildSAMLObject(Signature.class);

        signature.setSigningCredential(signingCredential);
        signature.setSignatureAlgorithm(StringUtils.hasText(signatureAlgorithm) ? signatureAlgorithm : SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA256);
        signature.setCanonicalizationAlgorithm(SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);

        signableXMLObject.setSignature(signature);

        XMLObjectProviderRegistrySupport.getMarshallerFactory().getMarshaller(signableXMLObject).marshall(signableXMLObject);
        Signer.signObject(signature);
    }

    public static String randomSAMLId() {
        return "_" + UUID.randomUUID();
    }

    private static AuthnStatement buildAuthnStatement(Instant authnInstant, String entityID, String authnContextClassRefValue) {
        AuthnContextClassRef authnContextClassRef = buildSAMLObject(AuthnContextClassRef.class);
        authnContextClassRef.setURI(StringUtils.hasText(authnContextClassRefValue) ? authnContextClassRefValue : AuthnContext.PASSWORD_AUTHN_CTX);

        AuthenticatingAuthority authenticatingAuthority = buildSAMLObject(AuthenticatingAuthority.class);
        authenticatingAuthority.setURI(entityID);

        AuthnContext authnContext = buildSAMLObject(AuthnContext.class);
        authnContext.setAuthnContextClassRef(authnContextClassRef);
        authnContext.getAuthenticatingAuthorities().add(authenticatingAuthority);

        AuthnStatement authnStatement = buildSAMLObject(AuthnStatement.class);
        authnStatement.setAuthnContext(authnContext);

        authnStatement.setAuthnInstant(authnInstant);

        return authnStatement;
    }

    private static AttributeStatement buildAttributeStatement(List<SAMLAttribute> attributes) {
        AttributeStatement attributeStatement = buildSAMLObject(AttributeStatement.class);

        attributes.forEach(entry ->
                attributeStatement.getAttributes().add(
                        buildAttribute(
                                entry.getName(),
                                entry.getValues())));

        return attributeStatement;
    }

    private static Attribute buildAttribute(String name, List<String> values) {
        XSStringBuilder stringBuilder = (XSStringBuilder) XMLObjectProviderRegistrySupport.getBuilderFactory().getBuilder(XSString.TYPE_NAME);

        Attribute attribute = buildSAMLObject(Attribute.class);
        attribute.setName(name);
        attribute.setNameFormat(Attribute.URI_REFERENCE);
        List<XSString> xsStringList = values.stream().map(value -> {
            XSString stringValue = stringBuilder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME, XSString.TYPE_NAME);
            stringValue.setValue(value);
            return stringValue;
        }).collect(toList());

        attribute.getAttributeValues().addAll(xsStringList);
        return attribute;
    }

}
