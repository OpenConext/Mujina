package mujina.api;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import mujina.saml.KeyStoreLocator;
import org.opensaml.xml.Configuration;
import org.opensaml.xml.security.BasicSecurityConfiguration;
import org.opensaml.xml.signature.SignatureConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.saml.key.JKSKeyManager;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.util.Enumeration;

@Getter
@Setter
public abstract class SharedConfiguration {

  @JsonIgnore
  protected static final Logger LOG = LoggerFactory.getLogger(SharedConfiguration.class);
  @JsonIgnore
  private JKSKeyManager keyManager;
  private String keystorePassword = "secret";
  private boolean needsSigning;
  private String defaultSignatureAlgorithm = SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA256;
  private String signatureAlgorithm;
  private String entityId;

  public SharedConfiguration(JKSKeyManager keyManager) {
    this.keyManager = keyManager;
  }

  public abstract void reset();

  public void setEntityId(String newEntityId, boolean addTokenToStore) {
    if (addTokenToStore) {
      try {
        KeyStore keyStore = keyManager.getKeyStore();
        KeyStore.PasswordProtection passwordProtection = new KeyStore.PasswordProtection(keystorePassword.toCharArray());
        KeyStore.Entry keyStoreEntry = keyStore.getEntry(this.entityId, passwordProtection);
        keyStore.setEntry(newEntityId, keyStoreEntry, passwordProtection);
      } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableEntryException e) {
        throw new RuntimeException(e);
      }
    }
    this.entityId = newEntityId;
  }

  public void injectCredential(final String certificate, final String pemKey) {
    try {
      KeyStore keyStore = keyManager.getKeyStore();
      if (keyStore.containsAlias(entityId)) {
        keyStore.deleteEntry(entityId);
      }
      KeyStoreLocator.addPrivateKey(keyStore, entityId, pemKey, certificate, keystorePassword);
    } catch (Exception e) {
      throw new RuntimeException("Unable to append signing credential", e);
    }
  }

  protected void resetKeyStore(String alias, String privateKey, String certificate) {
    try {
      KeyStore keyStore = keyManager.getKeyStore();
      Enumeration<String> aliases = keyStore.aliases();
      while (aliases.hasMoreElements()) {
        keyStore.deleteEntry(aliases.nextElement());
      }
      KeyStoreLocator.addPrivateKey(keyStore, alias, privateKey, certificate, getKeystorePassword());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void setSignatureAlgorithm(String signatureAlgorithm) {
    this.signatureAlgorithm = signatureAlgorithm;
    BasicSecurityConfiguration.class.cast(Configuration.getGlobalSecurityConfiguration()).registerSignatureAlgorithmURI("RSA", signatureAlgorithm);
  }
}
