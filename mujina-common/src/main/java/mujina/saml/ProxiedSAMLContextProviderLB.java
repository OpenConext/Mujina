package mujina.saml;

import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.springframework.security.saml.context.SAMLContextProviderLB;
import org.springframework.security.saml.context.SAMLMessageContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URI;

public class ProxiedSAMLContextProviderLB extends SAMLContextProviderLB {

  public ProxiedSAMLContextProviderLB(URI uri) {
    super();
    setServerName(uri.getHost());
    setScheme(uri.getScheme());
    setContextPath("");
    if (uri.getPort() > 0) {
      setIncludeServerPortInRequestURL(true);
      setServerPort(uri.getPort());
    }
  }

  @Override
  public void populateGenericContext(HttpServletRequest request, HttpServletResponse response, SAMLMessageContext context) throws MetadataProviderException {
    super.populateGenericContext(request, response, context);
  }

}
