package mujina.api.idp;

import lombok.Getter;
import lombok.Setter;
import mujina.api.SharedConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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

  private String HTTP_MOCK_IDP = "http://mock-idp";

  private Map<String, List<String>> attributes = new TreeMap<>();
  private List<UsernamePasswordAuthenticationToken> users = new ArrayList<>();
  private String acsEndpoint;
  private AuthenticationMethod authenticationMethod;
  private final String idpPrivateKey;
  private final String idpCertificate;

  public IdpConfiguration(@Value("${sp.private_key}") String idpPrivateKey,
                          @Value("${sp.certificate}") String idpCertificate) {
    this.idpPrivateKey = idpPrivateKey;
    this.idpCertificate = idpCertificate;
    reset();
  }

  @Override
  public void reset() {
    setEntityId(HTTP_MOCK_IDP);
    resetAttributes();
    resetKeyStore(HTTP_MOCK_IDP, idpPrivateKey, idpCertificate);
    resetUsers();
    setAcsEndpoint(null);
    setAuthenticationMethod(AuthenticationMethod.USER);
  }

  private void resetUsers() {
    users.clear();
    UsernamePasswordAuthenticationToken admin = new UsernamePasswordAuthenticationToken("admin", "secret", Arrays.asList(new SimpleGrantedAuthority("ROLE_USER"),
      new SimpleGrantedAuthority("ROLE_ADMIN")));
    UsernamePasswordAuthenticationToken user = new UsernamePasswordAuthenticationToken("user", "secret", Arrays.asList(new SimpleGrantedAuthority("ROLE_USER")));
    users.add(admin);
    users.add(user);
  }

  private void resetAttributes() {
    attributes.clear();
    putAttribute("urn:mace:dir:attribute-def:uid", "john.doe");
    putAttribute("urn:mace:dir:attribute-def:cn", "John Doe");
    putAttribute("urn:mace:dir:attribute-def:givenName", "John");
    putAttribute("urn:mace:dir:attribute-def:sn", "Doe");
    putAttribute("urn:mace:dir:attribute-def:displayName", "John Doe");
    putAttribute("urn:mace:dir:attribute-def:mail", "j.doe@example.com");
    putAttribute("urn:mace:terena.org:attribute-def:schacHomeOrganization", "example.com");
    putAttribute("urn:mace:dir:attribute-def:eduPersonPrincipalName", "j.doe@example.com");
    putAttribute("urn:oid:1.3.6.1.4.1.1076.20.100.10.10.1", "guest");
  }

  private void putAttribute(String key, String... values) {
    this.attributes.put(key, Arrays.asList(values));
  }

}
