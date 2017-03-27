package mujina.sp;

import mujina.api.SpConfiguration;
import org.opensaml.common.SAMLException;
import org.opensaml.saml2.metadata.AssertionConsumerService;
import org.opensaml.saml2.metadata.Endpoint;
import org.opensaml.saml2.metadata.SPSSODescriptor;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.opensaml.ws.message.encoder.MessageEncodingException;
import org.springframework.security.saml.context.SAMLMessageContext;
import org.springframework.security.saml.processor.SAMLBinding;
import org.springframework.security.saml.processor.SAMLProcessorImpl;

import java.util.Collection;

public class ConfigurableSAMLProcessor extends SAMLProcessorImpl {

  private final SpConfiguration spConfiguration;

  public ConfigurableSAMLProcessor(Collection<SAMLBinding> bindings, SpConfiguration spConfiguration) {
    super(bindings);
    this.spConfiguration = spConfiguration;
  }

  @Override
  public SAMLMessageContext sendMessage(SAMLMessageContext samlContext, boolean sign)
    throws SAMLException, MetadataProviderException, MessageEncodingException {

    Endpoint endpoint = samlContext.getPeerEntityEndpoint();

    SAMLBinding binding = getBinding(endpoint);

    samlContext.setLocalEntityId(spConfiguration.getEntityId());
    samlContext.getLocalEntityMetadata().setEntityID(spConfiguration.getEntityId());
    samlContext.getPeerEntityEndpoint().setLocation(spConfiguration.getIdpSSOServiceURL());

    SPSSODescriptor roleDescriptor = (SPSSODescriptor) samlContext.getLocalEntityMetadata().getRoleDescriptors().get(0);
    AssertionConsumerService assertionConsumerService = roleDescriptor.getAssertionConsumerServices().stream().filter(service -> service.isDefault()).findAny().orElseThrow(() -> new RuntimeException("No default ACS"));
    assertionConsumerService.setBinding(spConfiguration.getProtocolBinding());
    assertionConsumerService.setLocation(spConfiguration.getAssertionConsumerServiceURL());

    return super.sendMessage(samlContext, spConfiguration.isNeedsSigning(), binding);

  }
}
