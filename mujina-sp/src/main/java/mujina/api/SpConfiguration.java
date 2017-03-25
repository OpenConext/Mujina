package mujina.api;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.saml.key.JKSKeyManager;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
public class SpConfiguration extends SharedConfiguration {

  private String defaultEntityId;

  private String defaultIdpSSOServiceURL;
  private String idpSSOServiceURL;
  private String defaultProtocolBinding = "urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST";
  private String protocolBinding;
  private final String defaultAssertionConsumerServiceURL;
  private String assertionConsumerServiceURL;
  private final String spPrivateKey;
  private final String spCertificate;

  @Autowired
  public SpConfiguration(JKSKeyManager keyManager,
                         @Value("${sp.entity_id}") String defaultEntityId,
                         @Value("${sp.single_sign_on_service_location}") String defaultIdpSSOServiceURL,
                         @Value("${sp.acs_location}") String defaultAssertionConsumerServiceURL,
                         @Value("${sp.private_key}") String spPrivateKey,
                         @Value("${sp.certificate}") String spCertificate) {
    super(keyManager);
    this.defaultEntityId = defaultEntityId;
    this.defaultIdpSSOServiceURL = defaultIdpSSOServiceURL;
    this.defaultAssertionConsumerServiceURL = defaultAssertionConsumerServiceURL;
    this.protocolBinding = defaultProtocolBinding;
    this.spPrivateKey = spPrivateKey;
    this.spCertificate = spCertificate;
    reset();
  }

  @Override
  public void reset() {
    setEntityId(defaultEntityId, false);
    setNeedsSigning(false);
    resetKeyStore(defaultEntityId, spPrivateKey, spCertificate);
    idpSSOServiceURL = defaultIdpSSOServiceURL;
    protocolBinding = defaultProtocolBinding;
    assertionConsumerServiceURL = defaultAssertionConsumerServiceURL;
  }

}
