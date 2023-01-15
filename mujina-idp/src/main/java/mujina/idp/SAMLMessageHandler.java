package mujina.idp;

import mujina.api.IdpConfiguration;
import mujina.saml.ProxiedSAMLContextProviderLB;
import mujina.saml.SAMLBuilder;
import mujina.saml.SAMLPrincipal;
import org.joda.time.DateTime;
import org.opensaml.common.SAMLObject;
import org.opensaml.common.binding.BasicSAMLMessageContext;
import org.opensaml.common.binding.decoding.SAMLMessageDecoder;
import org.opensaml.common.binding.encoding.SAMLMessageEncoder;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.binding.encoding.HTTPRedirectDeflateEncoder;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.LogoutResponse;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.Status;
import org.opensaml.saml2.core.StatusCode;
import org.opensaml.saml2.core.StatusResponseType;
import org.opensaml.saml2.metadata.Endpoint;
import org.opensaml.saml2.metadata.SingleSignOnService;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.opensaml.ws.message.decoder.MessageDecodingException;
import org.opensaml.ws.message.encoder.MessageEncodingException;
import org.opensaml.ws.security.SecurityPolicyResolver;
import org.opensaml.ws.transport.http.HttpServletResponseAdapter;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.security.CriteriaSet;
import org.opensaml.xml.security.SecurityException;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.security.criteria.EntityIDCriteria;
import org.opensaml.xml.signature.SignatureException;
import org.opensaml.xml.validation.ValidationException;
import org.opensaml.xml.validation.ValidatorSuite;
import org.springframework.security.saml.context.SAMLMessageContext;
import org.springframework.security.saml.key.KeyManager;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.List;

import static java.util.Arrays.asList;
import static mujina.saml.SAMLBuilder.buildAssertion;
import static mujina.saml.SAMLBuilder.buildIssuer;
import static mujina.saml.SAMLBuilder.buildSAMLObject;
import static mujina.saml.SAMLBuilder.buildStatus;
import static mujina.saml.SAMLBuilder.signAssertion;
import static org.opensaml.xml.Configuration.getValidatorSuite;

public class SAMLMessageHandler {

  private final KeyManager keyManager;
  private final Collection<SAMLMessageDecoder> decoders;
  private final SAMLMessageEncoder encoder;
  private final SAMLMessageEncoder logoutEncoder;
  private final SecurityPolicyResolver resolver;
  private final IdpConfiguration idpConfiguration;

  private final List<ValidatorSuite> validatorSuites;
  private final ProxiedSAMLContextProviderLB proxiedSAMLContextProviderLB;

  public SAMLMessageHandler(KeyManager keyManager, Collection<SAMLMessageDecoder> decoders,
                            SAMLMessageEncoder encoder, SecurityPolicyResolver securityPolicyResolver,
                            IdpConfiguration idpConfiguration, String idpBaseUrl) throws URISyntaxException {
    this.keyManager = keyManager;
    this.encoder = encoder;
    this.decoders = decoders;
    this.resolver = securityPolicyResolver;
    this.idpConfiguration = idpConfiguration;
    this.validatorSuites = asList(
      getValidatorSuite("saml2-core-schema-validator"),
      getValidatorSuite("saml2-core-spec-validator"));
    this.proxiedSAMLContextProviderLB = new ProxiedSAMLContextProviderLB(new URI(idpBaseUrl));
    logoutEncoder = new HTTPRedirectDeflateEncoder();
  }

  public SAMLMessageContext extractSAMLMessageContext(HttpServletRequest request, HttpServletResponse response, boolean postRequest) throws ValidationException, SecurityException, MessageDecodingException, MetadataProviderException {
    SAMLMessageContext messageContext = new SAMLMessageContext();

    proxiedSAMLContextProviderLB.populateGenericContext(request, response, messageContext);

    messageContext.setSecurityPolicyResolver(resolver);

    SAMLMessageDecoder samlMessageDecoder = samlMessageDecoder(postRequest);
    samlMessageDecoder.decode(messageContext);

    SAMLObject inboundSAMLMessage = messageContext.getInboundSAMLMessage();

    //lambda is poor with Exceptions
    for (ValidatorSuite validatorSuite : validatorSuites) {
      validatorSuite.validate(inboundSAMLMessage);
    }
    return messageContext;
  }

  private SAMLMessageDecoder samlMessageDecoder(boolean postRequest) {
    return decoders.stream().filter(samlMessageDecoder -> postRequest ?
      samlMessageDecoder.getBindingURI().equals(SAMLConstants.SAML2_POST_BINDING_URI) :
      samlMessageDecoder.getBindingURI().equals(SAMLConstants.SAML2_REDIRECT_BINDING_URI))
      .findAny()
      .orElseThrow(() -> new RuntimeException(String.format("Only %s and %s are supported",
        SAMLConstants.SAML2_REDIRECT_BINDING_URI,
        SAMLConstants.SAML2_POST_BINDING_URI)));
  }

  @SuppressWarnings("unchecked")
  private void sendResponseCommon(SAMLMessageEncoder encoder, StatusResponseType responseObject,
                                  SAMLPrincipal principal, String statusCode,
                                  HttpServletResponse response) throws MessageEncodingException {

    Status status = buildStatus(statusCode);

    String entityId = idpConfiguration.getEntityId();
    Credential signingCredential = resolveCredential(entityId);

    Issuer issuer = buildIssuer(entityId);

    responseObject.setIssuer(issuer);
    responseObject.setID(SAMLBuilder.randomSAMLId());
    responseObject.setIssueInstant(new DateTime());
    responseObject.setInResponseTo(principal.getRequestID());

    responseObject.setDestination(principal.getAssertionConsumerServiceURL());

    responseObject.setStatus(status);

    Endpoint endpoint = buildSAMLObject(Endpoint.class, SingleSignOnService.DEFAULT_ELEMENT_NAME);
    endpoint.setLocation(principal.getAssertionConsumerServiceURL());

    HttpServletResponseAdapter outTransport = new HttpServletResponseAdapter(response, false);

    BasicSAMLMessageContext messageContext = new BasicSAMLMessageContext();

    messageContext.setOutboundMessageTransport(outTransport);
    messageContext.setPeerEntityEndpoint(endpoint);
    messageContext.setOutboundSAMLMessage(responseObject);
    messageContext.setOutboundSAMLMessageSigningCredential(signingCredential);

    messageContext.setOutboundMessageIssuer(entityId);
    messageContext.setRelayState(principal.getRelayState());

    encoder.encode(messageContext);

  }

  public void sendLogoutResponse(SAMLPrincipal principal, HttpServletResponse response, String statusCode) throws MessageEncodingException {

    LogoutResponse logoutResponse = buildSAMLObject(LogoutResponse.class, LogoutResponse.DEFAULT_ELEMENT_NAME);
    sendResponseCommon(logoutEncoder, logoutResponse, principal, statusCode, response);

  }

  public void sendAuthnResponse(SAMLPrincipal principal, HttpServletResponse response) throws MarshallingException, SignatureException, MessageEncodingException {

    String entityId = idpConfiguration.getEntityId();
    Credential signingCredential = resolveCredential(entityId);

    Response authResponse = buildSAMLObject(Response.class, Response.DEFAULT_ELEMENT_NAME);

    Assertion assertion = buildAssertion(principal, buildStatus(StatusCode.SUCCESS_URI), entityId);
    signAssertion(assertion, signingCredential);

    authResponse.getAssertions().add(assertion);

    sendResponseCommon(encoder, authResponse, principal, StatusCode.SUCCESS_URI, response);

  }

  private Credential resolveCredential(String entityId) {
    try {
      return keyManager.resolveSingle(new CriteriaSet(new EntityIDCriteria(entityId)));
    } catch (SecurityException e) {
      throw new RuntimeException(e);
    }
  }

}
