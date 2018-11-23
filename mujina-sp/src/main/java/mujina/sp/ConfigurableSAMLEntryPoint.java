package mujina.sp;

import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.opensaml.ws.transport.InTransport;
import org.opensaml.ws.transport.http.HttpServletRequestAdapter;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.saml.SAMLEntryPoint;
import org.springframework.security.saml.context.SAMLMessageContext;
import org.springframework.security.saml.websso.WebSSOProfileOptions;

public class ConfigurableSAMLEntryPoint extends SAMLEntryPoint {

  @Override
  protected WebSSOProfileOptions getProfileOptions(SAMLMessageContext context, AuthenticationException exception) throws MetadataProviderException {
    WebSSOProfileOptions profileOptions = super.getProfileOptions(context, exception);
    InTransport inboundMessageTransport = context.getInboundMessageTransport();
    if (inboundMessageTransport instanceof HttpServletRequestAdapter) {
      HttpServletRequestAdapter messageTransport = (HttpServletRequestAdapter) inboundMessageTransport;
      String forceAuthn = messageTransport.getParameterValue("force-authn");
      if (forceAuthn != null && "true".equals(forceAuthn)) {
        profileOptions.setForceAuthN(true);
      }
    }
    return profileOptions;
  }
}
