package nl.surfnet.mockoleth.model;

import java.security.KeyStore;
import java.util.Map;

public interface Configuration {
    void reset();
    Map<String, String> getAttributes();
    KeyStore getKeyStore();
}
