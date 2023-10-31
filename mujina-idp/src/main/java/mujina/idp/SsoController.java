package mujina.idp;

import mujina.api.IdpConfiguration;
import mujina.saml.SAMLAttribute;
import mujina.saml.SAMLPrincipal;
import org.opensaml.common.binding.SAMLMessageContext;
import org.opensaml.saml2.core.AuthnContext;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.core.NameIDType;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.opensaml.ws.message.decoder.MessageDecodingException;
import org.opensaml.ws.message.encoder.MessageEncodingException;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.security.SecurityException;
import org.opensaml.xml.signature.SignatureException;
import org.opensaml.xml.validation.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

@Controller
public class SsoController {

    @Autowired
    private SAMLMessageHandler samlMessageHandler;

    @Autowired
    private IdpConfiguration idpConfiguration;

    @GetMapping("/SingleSignOnService")
    public void singleSignOnServiceGet(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, MarshallingException, SignatureException, MessageEncodingException, ValidationException, SecurityException, MessageDecodingException, MetadataProviderException, ServletException {
        doSSO(request, response, authentication, false);
    }

    @PostMapping("/SingleSignOnService")
    public void singleSignOnServicePost(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, MarshallingException, SignatureException, MessageEncodingException, ValidationException, SecurityException, MessageDecodingException, MetadataProviderException, ServletException {
        doSSO(request, response, authentication, true);
    }

    @SuppressWarnings("unchecked")
    private void doSSO(HttpServletRequest request, HttpServletResponse response, Authentication authentication, boolean postRequest) throws ValidationException, SecurityException, MessageDecodingException, MarshallingException, SignatureException, MessageEncodingException, MetadataProviderException, IOException, ServletException {
        SAMLMessageContext messageContext = samlMessageHandler.extractSAMLMessageContext(request, response, postRequest);
        AuthnRequest authnRequest = (AuthnRequest) messageContext.getInboundSAMLMessage();

        String assertionConsumerServiceURL = idpConfiguration.getAcsEndpoint() != null ? idpConfiguration.getAcsEndpoint() : authnRequest.getAssertionConsumerServiceURL();
        Map<String, String[]> parameterMap = (Map<String, String[]>) authentication.getDetails();
        String[] authnContextClassRefs = parameterMap.get("authn-context-class-ref-value");

        List<SAMLAttribute> attributes = attributes(authentication);

        String authnContextClassRefValue = authnContextClassRefs != null ? authnContextClassRefs[0] : AuthnContext.PASSWORD_AUTHN_CTX;

        SAMLPrincipal principal = new SAMLPrincipal(
                authentication.getName(),
                attributes.stream()
                        .filter(attr -> "urn:oasis:names:tc:SAML:1.1:nameid-format".equals(attr.getName()))
                        .findFirst().map(attr -> attr.getValue()).orElse(NameIDType.UNSPECIFIED),
                attributes,
                authnRequest.getIssuer().getValue(),
                authnRequest.getID(),
                assertionConsumerServiceURL,
                messageContext.getRelayState());

        samlMessageHandler.sendAuthnResponse(principal, authnContextClassRefValue, response);
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
        if (parameterMap.containsKey("authn-context-class-ref-value")) {
            result.remove("authn-context-class-ref-value");
        }

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
        Map<String, String> standardAttributes = idpConfiguration.getStandardAttributes().getAttributes();
        Map<String, List<String>> replacements = new HashMap<>();
        String mail = String.format("%s@%s",
                        uid.replaceAll("[^a-zA-Z0-9]", ""),
                        "example.com")
                .toLowerCase();
        String givenName = uid.substring(0, 1).toUpperCase() + uid.substring(1);
        result.keySet().forEach(key -> {
            String standardValue = standardAttributes.get(key);
            List<String> resultValues = result.get(key);
            //Only override the attributes that were not entered by the user, e.g. equal the standard value
            if (StringUtils.hasText(standardValue) && !CollectionUtils.isEmpty(resultValues) &&
                    resultValues.get(0).equals(standardValue)) {
                switch (key) {
                    case "urn:mace:dir:attribute-def:cn":
                    case "urn:mace:dir:attribute-def:displayName":
                        replacements.put(key, List.of(givenName + " Doe"));
                        break;
                    case "urn:mace:dir:attribute-def:givenName":
                        replacements.put(key, List.of(givenName));
                        break;
                    case "urn:mace:dir:attribute-def:mail":
                    case "urn:mace:dir:attribute-def:eduPersonPrincipalName":
                    case "urn:oasis:names:tc:SAML:attribute:subject-id":
                        replacements.put(key, List.of(mail));
                        break;
                }
            }
        });
        result.putAll(replacements);
        //Provide the ability to limit the list attributes returned to the SP
        return result.entrySet().stream()
                .filter(entry -> !entry.getValue().stream().allMatch(StringUtils::isEmpty))
                .map(entry -> entry.getKey().equals("urn:mace:dir:attribute-def:uid") ?
                        new SAMLAttribute(entry.getKey(), singletonList(uid)) :
                        new SAMLAttribute(entry.getKey(), entry.getValue()))
                .collect(toList());
    }

}
