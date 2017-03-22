package mujina.api;

import lombok.Getter;
import lombok.Setter;
import mujina.saml.KeyStoreLocator;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.charset.Charset;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public abstract class SharedConfiguration {

  protected final Logger LOG = LoggerFactory.getLogger(getClass());

  private KeyStore keyStore;
  private String keystorePassword = "secret";
  private boolean needsSigning;
  private String signatureAlgorithm = "http://www.w3.org/2001/04/xmldsig-more#rsa-sha256";
  private String sloEndpoint;
  private String entityId;
  private Map<String, String> privateKeyPasswords = new HashMap<>();

  public abstract void reset();

  public void setEntityId(String newEntityId, boolean addTokenToStore) {
    if (addTokenToStore) {
      try {
         KeyStore.PasswordProtection passwordProtection = new KeyStore.PasswordProtection(keystorePassword.toCharArray());
         KeyStore.Entry keyStoreEntry = keyStore.getEntry(this.entityId, passwordProtection);
        keyStore.setEntry(newEntityId, keyStoreEntry, passwordProtection);
        privateKeyPasswords.put(newEntityId, keystorePassword);
      } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableEntryException e) {
        throw new RuntimeException(e);
      }
    }
    this.entityId = newEntityId;
  }

  public void injectCredential(final String certificate, final String pemKey) {
    try {
      if (keyStore.containsAlias(entityId)) {
        keyStore.deleteEntry(entityId);
      }
      String key = new String(Base64.decodeBase64(pemKey));
      KeyStoreLocator.addPrivateKey(keyStore, entityId, key, certificate, keystorePassword);
    } catch (Exception e) {
      throw new RuntimeException("Unable to append signing credential", e);
    }
  }

  protected void resetKeyStore(String alias, String privateKey, String certificate) {
    try {
      KeyStore keyStore = KeyStoreLocator.createKeyStore(getKeystorePassword());
      KeyStoreLocator.addPrivateKey(keyStore, alias, privateKey,
        certificate, getKeystorePassword());
      setKeyStore(keyStore);
      getPrivateKeyPasswords().put(alias, getKeystorePassword());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

}
