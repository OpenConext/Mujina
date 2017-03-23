package mujina.idp;

import mujina.api.AuthenticationMethod;
import mujina.api.IdpConfiguration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Arrays;

public class AuthenticationProvider implements org.springframework.security.authentication.AuthenticationProvider {

  private final IdpConfiguration idpConfiguration;

  public AuthenticationProvider(IdpConfiguration idpConfiguration) {
    this.idpConfiguration = idpConfiguration;
  }

  @Override
  public Authentication authenticate(Authentication authentication) throws AuthenticationException {
    if (idpConfiguration.getAuthenticationMethod().equals(AuthenticationMethod.ALL)) {
      return new UsernamePasswordAuthenticationToken(authentication.getPrincipal(), authentication.getCredentials(), Arrays.asList(
        new SimpleGrantedAuthority("ADMIN"), new SimpleGrantedAuthority("USER")
      ));
    } else {
      return idpConfiguration.getUsers().stream()
        .filter(token ->
          token.getPrincipal().equals(authentication.getPrincipal()) &&
            token.getCredentials().equals(authentication.getCredentials()))
        .findFirst()
        .orElseThrow(() -> new AuthenticationException("User not found or bad credentials") {
        });
    }
  }

  @Override
  public boolean supports(Class<?> authentication) {
    return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
  }
}
