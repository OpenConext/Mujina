package mujina.sp;

import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.springframework.security.saml.context.SAMLMessageContext;
import org.springframework.security.saml.metadata.MetadataDisplayFilter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class DefaultMetadataDisplayFilter extends MetadataDisplayFilter {

  @Override
  protected void processMetadataDisplay(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    try {
      SAMLMessageContext context = contextProvider.getLocalEntity(request, response);
      String entityId = context.getLocalEntityId();
      response.setContentType("application/xml");
      displayMetadata(entityId, response.getWriter());
    } catch (MetadataProviderException e) {
      throw new ServletException("Error initializing metadata", e);
    }
  }
}
