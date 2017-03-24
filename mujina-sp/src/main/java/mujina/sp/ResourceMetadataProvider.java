package mujina.sp;

import org.opensaml.saml2.metadata.provider.AbstractMetadataProvider;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.UnmarshallingException;
import org.springframework.core.io.Resource;

import java.io.IOException;

public class ResourceMetadataProvider extends AbstractMetadataProvider {

  private final Resource resource;

  public ResourceMetadataProvider(Resource resource) {
    this.resource = resource;
  }

  @Override
  protected XMLObject doGetMetadata() throws MetadataProviderException {
    try {
      return super.unmarshallMetadata(resource.getInputStream());
    } catch (UnmarshallingException | IOException e) {
      throw new MetadataProviderException(e);
    }
  }
}
