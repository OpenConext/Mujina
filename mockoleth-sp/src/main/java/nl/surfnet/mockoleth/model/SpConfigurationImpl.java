package nl.surfnet.mockoleth.model;


import java.security.KeyStore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpConfigurationImpl extends CommonConfigurationImpl implements SpConfiguration {

    private final static Logger log = LoggerFactory
            .getLogger(SpConfigurationImpl.class);

    public SpConfigurationImpl() {
        reset();
    }

    @Override
    public void reset() {
        entityId = "sp";
        try {
            keyStore = KeyStore.getInstance("JKS");
            keyStore.load(null, keystorePassword.toCharArray());
            appendToKeyStore(keyStore, "idp", "idp-crt.pem", "idp-key.pkcs8.der", keystorePassword.toCharArray());
            appendToKeyStore(keyStore, "sp", "idp-crt.pem", "idp-key.pkcs8.der", keystorePassword.toCharArray());
            privateKeyPasswords.put("idp", keystorePassword);
            privateKeyPasswords.put("sp", keystorePassword);
        } catch (Exception e) {
            log.error("Unable to create default keystore", e);
        }
    }
}
