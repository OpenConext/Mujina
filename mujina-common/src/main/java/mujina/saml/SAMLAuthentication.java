package mujina.saml;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;

public class SAMLAuthentication extends AbstractAuthenticationToken {

  private final SAMLPrincipal principal;

  public SAMLAuthentication(SAMLPrincipal principal) {
    super(AuthorityUtils.createAuthorityList("ROLE_USER"));
    this.principal = principal;
    setAuthenticated(true);
  }

  @Override
  public Object getCredentials() {
    return "N/A";
  }

  @Override
  public Object getPrincipal() {
    return principal;
  }
}
