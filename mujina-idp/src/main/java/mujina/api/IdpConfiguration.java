package mujina.api;

import lombok.Getter;
import lombok.Setter;
import mujina.config.StandardAttributes;
import mujina.idp.FederatedUserAuthenticationToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.saml.key.JKSKeyManager;
import org.springframework.stereotype.Component;

import java.util.*;

@Getter
@Setter
@Component
public class IdpConfiguration extends SharedConfiguration {

    private String defaultEntityId;
    private Map<String, List<String>> attributes = new TreeMap<>();
    private List<FederatedUserAuthenticationToken> users = new ArrayList<>();
    private String acsEndpoint;
    private AuthenticationMethod authenticationMethod;
    private AuthenticationMethod defaultAuthenticationMethod;
    private final String idpPrivateKey;
    private final String idpCertificate;
    private final StandardAttributes standardAttributes;

    @Autowired
    public IdpConfiguration(JKSKeyManager keyManager,
                            @Value("${idp.entity_id}") String defaultEntityId,
                            @Value("${idp.private_key}") String idpPrivateKey,
                            @Value("${idp.certificate}") String idpCertificate,
                            @Value("${idp.auth_method}") String authMethod,
                            StandardAttributes standardAttributes) {

        super(keyManager);
        this.defaultEntityId = defaultEntityId;
        this.idpPrivateKey = idpPrivateKey;
        this.idpCertificate = idpCertificate;
        this.defaultAuthenticationMethod = AuthenticationMethod.valueOf(authMethod);
        this.standardAttributes = standardAttributes;
        reset();
    }

    @Override
    public void reset() {
        setEntityId(defaultEntityId);
        resetAttributes();
        resetKeyStore(defaultEntityId, idpPrivateKey, idpCertificate);
        resetUsers();
        setAcsEndpoint(null);
        setAuthenticationMethod(this.defaultAuthenticationMethod);
        setSignatureAlgorithm(getDefaultSignatureAlgorithm());
    }

    private void resetUsers() {
        users.clear();
        users.addAll(Arrays.asList(
                new FederatedUserAuthenticationToken("admin", "secret", Arrays.asList(new SimpleGrantedAuthority("ROLE_USER"),
                        new SimpleGrantedAuthority("ROLE_ADMIN"))),
                new FederatedUserAuthenticationToken("user", "secret", Arrays.asList(new SimpleGrantedAuthority("ROLE_USER")))));
    }

    private void resetAttributes() {
        Map<String, String> configuredAttributes = standardAttributes.getAttributes();

        attributes.clear();
        for (Map.Entry<String, String> attribute : configuredAttributes.entrySet()) {
            putAttribute(attribute.getKey(), attribute.getValue());
        }

    }

    private void putAttribute(String key, String... values) {
        this.attributes.put(key, Arrays.asList(values));
    }

}
