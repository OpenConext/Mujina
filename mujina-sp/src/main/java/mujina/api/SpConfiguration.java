package mujina.api;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
  private String defaultProtocolBinding;
  private String protocolBinding;
  private String defaultAssertionConsumerServiceURL;
  private boolean defaultNeedsSigning;
  private String assertionConsumerServiceURL;
  private String spPrivateKey;
  private String spCertificate;

  @Autowired
  public SpConfiguration(JKSKeyManager keyManager,
                         @Value("${sp.base_url}") String spBaseUrl,
                         @Value("${sp.entity_id}") String defaultEntityId,
                         @Value("${sp.single_sign_on_service_location}") String defaultIdpSSOServiceURL,
                         @Value("${sp.acs_location_path}") String defaultAssertionConsumerServiceURLPath,
                         @Value("${sp.protocol_binding}") String defaultProtocolBinding,
                         @Value("${sp.private_key}") String spPrivateKey,
                         @Value("${sp.certificate}") String spCertificate,
                         @Value("${sp.needs_signing}") boolean needsSigning) {
    super(keyManager);
    this.setDefaultEntityId(defaultEntityId);
    this.setDefaultIdpSSOServiceURL(defaultIdpSSOServiceURL);
    this.setDefaultAssertionConsumerServiceURL(spBaseUrl + defaultAssertionConsumerServiceURLPath);
    this.setDefaultProtocolBinding(defaultProtocolBinding);
    this.setSpPrivateKey(spPrivateKey);
    this.setSpCertificate(spCertificate);
    this.setDefaultNeedsSigning(needsSigning);
    reset();
  }

  @Override
  public void reset() {
    setEntityId(defaultEntityId, false);
    setNeedsSigning(defaultNeedsSigning);
    resetKeyStore(defaultEntityId, spPrivateKey, spCertificate);
    setIdpSSOServiceURL(defaultIdpSSOServiceURL);
    setProtocolBinding(defaultProtocolBinding);
    setAssertionConsumerServiceURL(defaultAssertionConsumerServiceURL);
    setSignatureAlgorithm(getDefaultSignatureAlgorithm());
  }

}
