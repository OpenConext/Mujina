package mujina.idp;

import mujina.api.IdpConfiguration;
import mujina.saml.SAMLAttribute;
import mujina.saml.SAMLPrincipal;
import org.opensaml.common.binding.SAMLMessageContext;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.core.NameIDType;
import org.opensaml.ws.message.encoder.MessageEncodingException;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.security.SecurityException;
import org.opensaml.xml.signature.SignatureException;
import org.opensaml.xml.validation.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Controller
public class SsoController {

  @Autowired
  private SAMLMessageHandler samlMessageHandler;

  @Autowired
  private IdpConfiguration idpConfiguration;

  //@PostMapping("/SingleSignOnService")
  @GetMapping("/SingleSignOnService")
  public void singleSignOnService(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {

    //The SAMLRequest parameters are urlEncoded and the extraction expects unencoded parameters
    SAMLMessageContext messageContext = null;
    try {
      messageContext = samlMessageHandler.extractSAMLMessageContext(request);
      //messageContext = samlMessageHandler.extractSAMLMessageContext(new ParameterDecodingHttpServletRequestWrapper(request));
    } catch (ValidationException | SecurityException e) {
      throw new RuntimeException(e);
    }

    AuthnRequest authnRequest = (AuthnRequest) messageContext.getInboundSAMLMessage();

    SAMLPrincipal principal = new SAMLPrincipal(
      authentication.getName(),
      NameIDType.UNSPECIFIED,
      idpConfiguration.getAttributes().entrySet().stream().map(entry -> new SAMLAttribute(entry.getKey(), entry.getValue())).collect(toList()),
      authnRequest.getIssuer().getValue(),
      authnRequest.getID(),
      authnRequest.getAssertionConsumerServiceURL(),
      messageContext.getRelayState());

    try {
      samlMessageHandler.sendAuthnResponse(principal, response);
    } catch (MarshallingException | SignatureException | MessageEncodingException e) {
      throw new RuntimeException(e);
    }

  }

}
