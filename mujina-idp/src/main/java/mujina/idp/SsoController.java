package mujina.idp;

import mujina.api.IdpConfiguration;
import mujina.saml.SAMLAttribute;
import mujina.saml.SAMLPrincipal;
import org.opensaml.common.binding.SAMLMessageContext;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.core.LogoutRequest;
import org.opensaml.saml2.core.NameIDType;
import org.opensaml.saml2.core.RequestAbstractType;
import org.opensaml.saml2.core.StatusCode;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.opensaml.ws.message.decoder.MessageDecodingException;
import org.opensaml.ws.message.encoder.MessageEncodingException;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.security.SecurityException;
import org.opensaml.xml.signature.SignatureException;
import org.opensaml.xml.validation.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

@Controller
public class SsoController {

  protected final Logger LOG = LoggerFactory.getLogger(getClass());

  @Autowired
  private SAMLMessageHandler samlMessageHandler;

  @Autowired
  private IdpConfiguration idpConfiguration;

  @GetMapping("/SingleSignOnService")
  public void singleSignOnServiceGet(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
    throws MarshallingException, SignatureException, MessageEncodingException, ValidationException, SecurityException, MessageDecodingException, MetadataProviderException {
    doSSO(request, response, authentication, false);
  }

  @PostMapping("/SingleSignOnService")
  public void singleSignOnServicePost(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
    throws MarshallingException, SignatureException, MessageEncodingException, ValidationException, SecurityException, MessageDecodingException, MetadataProviderException {
    doSSO(request, response, authentication, true);
  }

  @GetMapping("/SingleLogoutService")
  public void singleLogoutServiceGet(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
    throws MessageEncodingException, ValidationException, SecurityException, MessageDecodingException, MetadataProviderException {
    doSLO(request, response, authentication, false);
  }

  @PostMapping("/SingleLogoutService")
  public void singleLogoutServicePost(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
    throws MessageEncodingException, ValidationException, SecurityException, MessageDecodingException, MetadataProviderException {
    doSLO(request, response, authentication, true);
  }

  private void doSSO(HttpServletRequest request, HttpServletResponse response, Authentication authentication, boolean postRequest) throws ValidationException, SecurityException, MessageDecodingException, MarshallingException, SignatureException, MessageEncodingException, MetadataProviderException {
    SAMLMessageContext messageContext = samlMessageHandler.extractSAMLMessageContext(request, response, postRequest);
    AuthnRequest authnRequest = (AuthnRequest) messageContext.getInboundSAMLMessage();

    String assertionConsumerServiceURL = idpConfiguration.getAcsEndpoint() != null ? idpConfiguration.getAcsEndpoint() : authnRequest.getAssertionConsumerServiceURL();

    samlMessageHandler.sendAuthnResponse(makeSAMLPrincipal(authentication, authnRequest, assertionConsumerServiceURL, messageContext.getRelayState()), response);

  }

  private SAMLPrincipal makeSAMLPrincipal(Authentication authentication, RequestAbstractType request,
                                          String assertionConsumerServiceURL, String relayState) {

    List<SAMLAttribute> attributes = attributes(authentication);

    return new SAMLPrincipal(
      authentication.getName(),
      attributes.stream()
        .filter(attr -> "urn:oasis:names:tc:SAML:1.1:nameid-format".equals(attr.getName()))
        .findFirst().map(SAMLAttribute::getValue).orElse(NameIDType.UNSPECIFIED),
      attributes,
      request.getIssuer().getValue(),
      request.getID(),
      assertionConsumerServiceURL,
      relayState);

  }

  private void doSLO(HttpServletRequest request, HttpServletResponse response, Authentication authentication, boolean postRequest)
    throws ValidationException, SecurityException, MessageDecodingException, MessageEncodingException, MetadataProviderException {

    SAMLMessageContext messageContext = samlMessageHandler.extractSAMLMessageContext(request, response, postRequest);
    LogoutRequest logoutRequest = (LogoutRequest) messageContext.getInboundSAMLMessage();

    // There is no SLS endpoint specified in the logout request, so the only
    // thing we can use is the SLS from the IDP configuration.
    String destination = idpConfiguration.getSlsEndpoint();

    if (!Objects.equals(authentication.getPrincipal(), logoutRequest.getNameID().getValue())) {

      LOG.warn("User "+authentication.getPrincipal()+" sent logout request for "+logoutRequest.getNameID());

      samlMessageHandler.sendLogoutResponse(makeSAMLPrincipal(authentication, logoutRequest,
        destination, messageContext.getRelayState()),
        response, StatusCode.NO_AUTHN_CONTEXT_URI);
      return;
    }

    LOG.warn("Logging out " + authentication.getPrincipal());

    HttpSession session = request.getSession(false);
    SecurityContextHolder.clearContext();
    if (session != null) {
      session.invalidate();
    }

    samlMessageHandler.sendLogoutResponse(makeSAMLPrincipal(authentication, logoutRequest,
      destination, messageContext.getRelayState()),
      response, StatusCode.SUCCESS_URI);

  }

  @SuppressWarnings("unchecked")
  private List<SAMLAttribute> attributes(Authentication authentication) {
    String uid = authentication.getName();
    Map<String, List<String>> result = new HashMap<>(idpConfiguration.getAttributes());


    Optional<Map<String, List<String>>> optionalMap = idpConfiguration.getUsers().stream()
      .filter(user -> user.getPrincipal().equals(uid))
      .findAny()
      .map(FederatedUserAuthenticationToken::getAttributes);
    optionalMap.ifPresent(result::putAll);

    //See SAMLAttributeAuthenticationFilter#setDetails
    Map<String, String[]> parameterMap = (Map<String, String[]>) authentication.getDetails();
    parameterMap.forEach((key, values) -> {
      result.put(key, Arrays.asList(values));
    });

    //Check if the user wants to be persisted
    if (parameterMap.containsKey("persist-me") && "on".equalsIgnoreCase(parameterMap.get("persist-me")[0])) {
      result.remove("persist-me");
      FederatedUserAuthenticationToken token = new FederatedUserAuthenticationToken(
        uid,
        authentication.getCredentials(),
        Arrays.asList(new SimpleGrantedAuthority("ROLE_USER")));
      token.setAttributes(result);
      idpConfiguration.getUsers().removeIf(existingUser -> existingUser.getPrincipal().equals(uid));
      idpConfiguration.getUsers().add(token);
    }

    //Provide the ability to limit the list attributes returned to the SP
    return result.entrySet().stream()
      .filter(entry -> !entry.getValue().stream().allMatch(StringUtils::isEmpty))
      .map(entry -> entry.getKey().equals("urn:mace:dir:attribute-def:uid") ?
        new SAMLAttribute(entry.getKey(), singletonList(uid)) :
        new SAMLAttribute(entry.getKey(), entry.getValue()))
      .collect(toList());
  }

}
