package mujina.api;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.saml.key.JKSKeyManager;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Getter
@Setter
@Component
public class IdpConfiguration extends SharedConfiguration {

  private String defaultEntityId;
  private Map<String, List<String>> attributes = new TreeMap<>();
  private List<UsernamePasswordAuthenticationToken> users = new ArrayList<>();
  private String acsEndpoint;
  private AuthenticationMethod authenticationMethod;
  private AuthenticationMethod defaultAuthenticationMethod;
  private final String idpPrivateKey;
  private final String idpCertificate;

  @Autowired
  public IdpConfiguration(JKSKeyManager keyManager,
                          @Value("${idp.entity_id}") String defaultEntityId,
                          @Value("${idp.private_key}") String idpPrivateKey,
                          @Value("${idp.certificate}") String idpCertificate,
                          @Value("${idp.auth_method}") String authMethod) {
    super(keyManager);
    this.defaultEntityId = defaultEntityId;
    this.idpPrivateKey = idpPrivateKey;
    this.idpCertificate = idpCertificate;
    this.defaultAuthenticationMethod = AuthenticationMethod.valueOf(authMethod);
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
        new UsernamePasswordAuthenticationToken("admin", "secret", Arrays.asList(new SimpleGrantedAuthority("ROLE_USER"),
            new SimpleGrantedAuthority("ROLE_ADMIN"))),
        new UsernamePasswordAuthenticationToken("j.doe@example.com", "secret", Arrays.asList(new SimpleGrantedAuthority("ROLE_USER"),
            new SimpleGrantedAuthority("ROLE_ADMIN"))),
      new UsernamePasswordAuthenticationToken("user", "secret", Arrays.asList(new SimpleGrantedAuthority("ROLE_USER")))));
  }

  private void resetAttributes() {
    attributes.clear();
    putAttribute("urn:oid:2.5.4.3", "Jon");
    putAttribute("urn:oid:0.9.2342.19200300.100.1.3", "j.doe@example.com");
    putAttribute("urn:oid:2.16.840.1.113730.3.1.241", "Jonathan Doe");

    putAttribute("urn:mace:dir:attribute-def:uid", "john.doe");
    putAttribute("urn:mace:dir:attribute-def:cn", "John Doe");
    putAttribute("urn:mace:dir:attribute-def:givenName", "John");
    putAttribute("urn:mace:dir:attribute-def:sn", "Doe");
    putAttribute("urn:mace:dir:attribute-def:displayName", "John Doe");
    putAttribute("urn:mace:dir:attribute-def:mail", "j.doe@example.com");
    putAttribute("urn:mace:terena.org:attribute-def:schacHomeOrganization", "example.com");
    putAttribute("urn:mace:dir:attribute-def:eduPersonPrincipalName", "j.doe@example.com");
  }

  private void putAttribute(String key, String... values) {
    this.attributes.put(key, Arrays.asList(values));
  }

}
