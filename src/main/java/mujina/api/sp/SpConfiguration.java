package mujina.api.sp;

import lombok.Getter;
import lombok.Setter;
import mujina.api.SharedConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
public class SpConfiguration extends SharedConfiguration {

  private String HTTP_MOCK_SP = "http://mock-sp";

  private String defaultIdpSSOServiceURL;
  private String idpSSOServiceURL;
  private String defaultProtocolBinding = "urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST";
  private String protocolBinding;
  private final String defaultAssertionConsumerServiceURL;
  private String assertionConsumerServiceURL;
  private final String spPrivateKey;
  private final String spCertificate;


  public SpConfiguration(@Value("${sp.single_sign_on_service_location}") String defaultIdpSSOServiceURL,
                         @Value("${sp.acs_location}") String defaultAssertionConsumerServiceURL,
                         @Value("${sp.private_key}") String spPrivateKey,
                         @Value("${sp.certificate}") String spCertificate) {
    this.defaultIdpSSOServiceURL = defaultIdpSSOServiceURL;
    this.defaultAssertionConsumerServiceURL = defaultAssertionConsumerServiceURL;
    this.protocolBinding = defaultProtocolBinding;
    this.spPrivateKey = spPrivateKey;
    this.spCertificate = spCertificate;
    reset();
  }

  @Override
  public void reset() {
    setEntityId(HTTP_MOCK_SP, false);
    setNeedsSigning(false);
    resetKeyStore(HTTP_MOCK_SP, spPrivateKey, spCertificate);
    idpSSOServiceURL = defaultIdpSSOServiceURL;
    protocolBinding = defaultProtocolBinding;
    assertionConsumerServiceURL = defaultAssertionConsumerServiceURL;
  }

}
