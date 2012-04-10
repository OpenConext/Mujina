package nl.surfnet.mockoleth.spring.security;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;

import nl.surfnet.mockoleth.model.IdpConfiguration;

public class CustomAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    private IdpConfiguration idpConfiguration;
    
    @Override
    public Authentication authenticate(final Authentication authentication) throws AuthenticationException {
        final String name = authentication.getName();
        final String password = authentication.getCredentials().toString();
        final Collection<UsernamePasswordAuthenticationToken> users = idpConfiguration.getUsers();
        for (UsernamePasswordAuthenticationToken user : users) {
            UserDetails principal = (UserDetails)user.getPrincipal();
            if (principal.getUsername().equals(name)
                    && principal.getPassword().equals(password)) {
                return user;
            }
        }
        return null;
    }

    @Override
    public boolean supports(final Class method) {
        return method.equals(UsernamePasswordAuthenticationToken.class);
    }
}
