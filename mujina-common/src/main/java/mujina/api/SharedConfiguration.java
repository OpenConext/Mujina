package mujina.api;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import mujina.saml.KeyStoreLocator;
import org.opensaml.core.config.InitializationService;
import org.opensaml.xmlsec.SecurityConfigurationSupport;
import org.opensaml.xmlsec.impl.BasicSignatureSigningConfiguration;
import org.opensaml.xmlsec.signature.support.SignatureConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.util.Enumeration;
import java.util.List;

@Getter
@Setter
public abstract class SharedConfiguration {

    @JsonIgnore
    protected static final Logger LOG = LoggerFactory.getLogger(SharedConfiguration.class);
    @JsonIgnore
    private KeyStore keyStore;
    private String keystorePassword = "secret";
    private boolean needsSigning;
    private String defaultSignatureAlgorithm = SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA256;
    private String signatureAlgorithm;
    private String entityId;

    public SharedConfiguration(KeyStore keyStore) {
        this.keyStore = keyStore;
    }

    public abstract void reset();

    public void setEntityId(String newEntityId, boolean addTokenToStore) {
        if (addTokenToStore) {
            try {
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
        try {
            //Idempotent: safe (and cheap) to call even if OpenSAML was already bootstrapped elsewhere.
            InitializationService.initialize();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        BasicSignatureSigningConfiguration config = (BasicSignatureSigningConfiguration)
                SecurityConfigurationSupport.getGlobalSignatureSigningConfiguration();
        config.setSignatureAlgorithms(List.of(signatureAlgorithm));
    }
}
