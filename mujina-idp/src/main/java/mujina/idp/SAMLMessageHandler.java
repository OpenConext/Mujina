package mujina.idp;

import mujina.saml.SAMLPrincipal;
import org.joda.time.DateTime;
import org.opensaml.common.SAMLObject;
import org.opensaml.common.binding.BasicSAMLMessageContext;
import org.opensaml.common.binding.SAMLMessageContext;
import org.opensaml.common.binding.decoding.SAMLMessageDecoder;
import org.opensaml.common.binding.encoding.SAMLMessageEncoder;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.Status;
import org.opensaml.saml2.core.StatusCode;
import org.opensaml.saml2.metadata.Endpoint;
import org.opensaml.saml2.metadata.SingleSignOnService;
import org.opensaml.security.SAMLSignatureProfileValidator;
import org.opensaml.ws.message.decoder.MessageDecodingException;
import org.opensaml.ws.message.encoder.MessageEncodingException;
import org.opensaml.ws.security.SecurityPolicyResolver;
import org.opensaml.ws.transport.http.HttpServletRequestAdapter;
import org.opensaml.ws.transport.http.HttpServletResponseAdapter;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.security.CriteriaSet;
import org.opensaml.xml.security.SecurityException;
import org.opensaml.xml.security.SigningUtil;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.security.criteria.EntityIDCriteria;
import org.opensaml.xml.signature.Signature;
import org.opensaml.xml.signature.SignatureException;
import org.opensaml.xml.signature.SignatureValidator;
import org.opensaml.xml.util.Base64;
import org.opensaml.xml.validation.ValidationException;
import org.opensaml.xml.validation.ValidatorSuite;
import org.springframework.security.saml.key.KeyManager;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.UUID;

import static java.util.Arrays.asList;
import static mujina.saml.SAMLBuilder.buildAssertion;
import static mujina.saml.SAMLBuilder.buildIssuer;
import static mujina.saml.SAMLBuilder.buildSAMLObject;
import static mujina.saml.SAMLBuilder.buildStatus;
import static mujina.saml.SAMLBuilder.signAssertion;
import static org.opensaml.xml.Configuration.getValidatorSuite;

public class SAMLMessageHandler {

  private final KeyManager keyManager;
  private final SAMLMessageEncoder encoder;
  private final SAMLMessageDecoder decoder;
  private final SecurityPolicyResolver resolver;
  private final String entityId;

  private final List<ValidatorSuite> validatorSuites;

  public SAMLMessageHandler(KeyManager keyManager, SAMLMessageDecoder samlMessageDecoder,
                            SAMLMessageEncoder samlMessageEncoder, SecurityPolicyResolver securityPolicyResolver,
                            String entityId) {
    this.keyManager = keyManager;
    this.encoder = samlMessageEncoder;
    this.decoder = samlMessageDecoder;
    this.resolver = securityPolicyResolver;
    this.entityId = entityId;
    this.validatorSuites = asList(
      getValidatorSuite("saml2-core-schema-validator"),
      getValidatorSuite("saml2-core-spec-validator"));
  }

  public SAMLMessageContext extractSAMLMessageContext(HttpServletRequest request) throws ValidationException, SecurityException {
    BasicSAMLMessageContext messageContext = new BasicSAMLMessageContext();

    messageContext.setInboundMessageTransport(new HttpServletRequestAdapter(request));
    messageContext.setSecurityPolicyResolver(resolver);

    try {
      decoder.decode(messageContext);
    } catch (SecurityException | MessageDecodingException e) {
      throw new RuntimeException(e);
    }

    SAMLObject inboundSAMLMessage = messageContext.getInboundSAMLMessage();
    if (!(inboundSAMLMessage instanceof AuthnRequest)) {
      throw new RuntimeException("Expected inboundSAMLMessage to be AuthnRequest, but actual " + inboundSAMLMessage.getClass());
    }

    AuthnRequest authnRequest = (AuthnRequest) inboundSAMLMessage;
    validate(request, authnRequest);
    return messageContext;
  }

  public void sendAuthnResponse(SAMLPrincipal principal, HttpServletResponse response) throws MarshallingException, SignatureException, MessageEncodingException {
    doSendAuthnResponse(principal, response, buildStatus(StatusCode.SUCCESS_URI));
  }

  public void sendFailedAuthnResponse(SAMLPrincipal samlPrincipal, HttpServletResponse response, String message) throws MarshallingException, SignatureException, MessageEncodingException {
    doSendAuthnResponse(samlPrincipal, response, buildStatus(StatusCode.RESPONDER_URI, StatusCode.AUTHN_FAILED_URI, message));
  }

  @SuppressWarnings("unchecked")
  private void doSendAuthnResponse(SAMLPrincipal principal, HttpServletResponse response, Status status) throws MarshallingException, SignatureException, MessageEncodingException {
    Credential signingCredential = resolveCredential(entityId);

    Response authResponse = buildSAMLObject(Response.class, Response.DEFAULT_ELEMENT_NAME);
    Issuer issuer = buildIssuer(entityId);

    authResponse.setIssuer(issuer);
    authResponse.setID(UUID.randomUUID().toString());
    authResponse.setIssueInstant(new DateTime());
    authResponse.setInResponseTo(principal.getRequestID());

    Assertion assertion = buildAssertion(principal, status, entityId);
    signAssertion(assertion, signingCredential);

    authResponse.getAssertions().add(assertion);
    authResponse.setDestination(principal.getAssertionConsumerServiceURL());

    authResponse.setStatus(status);

    Endpoint endpoint = buildSAMLObject(Endpoint.class, SingleSignOnService.DEFAULT_ELEMENT_NAME);
    endpoint.setLocation(principal.getAssertionConsumerServiceURL());

    HttpServletResponseAdapter outTransport = new HttpServletResponseAdapter(response, false);

    BasicSAMLMessageContext messageContext = new BasicSAMLMessageContext();

    messageContext.setOutboundMessageTransport(outTransport);
    messageContext.setPeerEntityEndpoint(endpoint);
    messageContext.setOutboundSAMLMessage(authResponse);
    messageContext.setOutboundSAMLMessageSigningCredential(signingCredential);

    messageContext.setOutboundMessageIssuer(entityId);
    messageContext.setRelayState(principal.getRelayState());

    encoder.encode(messageContext);
  }

  private void validate(HttpServletRequest request, AuthnRequest authnRequest) throws ValidationException, SecurityException {
    validateXMLObject(authnRequest);
    validateSignature(authnRequest);
  //  validateRawSignature(request, authnRequest.getIssuer().getValue());
  }

  private void validateXMLObject(XMLObject xmlObject) throws ValidationException {
    //lambda is poor with Exceptions
    for (ValidatorSuite validatorSuite : validatorSuites) {
      validatorSuite.validate(xmlObject);
    }
  }

//  private void validateRawSignature(HttpServletRequest request, String issuer) throws SecurityException {
//    String base64signature = request.getParameter("Signature");
//    String sigAlg = request.getParameter("SigAlg");
//    if (base64signature == null || sigAlg == null) {
//      return;
//    }
//    byte[] input = request.getQueryString().replaceFirst("&Signature[^&]+", "").getBytes();
//    byte[] signature = Base64.decode(base64signature);
//
//    Credential credential = resolveCredential(issuer);
//    SigningUtil.verifyWithURI(credential, sigAlg, signature, input);
//  }

  private void validateSignature(AuthnRequest authnRequest) throws ValidationException {
    Signature signature = authnRequest.getSignature();
    if (signature == null) {
      return;
    }
    new SAMLSignatureProfileValidator().validate(signature);
    String issuer = authnRequest.getIssuer().getValue();
    Credential credential = resolveCredential(issuer);
    new SignatureValidator(credential).validate(signature);
  }

  private Credential resolveCredential(String entityId) {
    try {
      return keyManager.resolveSingle(new CriteriaSet(new EntityIDCriteria(entityId)));
    } catch (SecurityException e) {
      throw new RuntimeException(e);
    }
  }

}
