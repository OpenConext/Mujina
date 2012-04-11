package nl.surfnet.mockoleth.spring.security;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import nl.surfnet.mockoleth.model.IdpConfiguration;
import nl.surfnet.mockoleth.model.SimpleAuthentication;

public class CustomAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    private IdpConfiguration idpConfiguration;
    
    @Override
    public Authentication authenticate(final Authentication authentication) throws AuthenticationException {
        final String name = authentication.getName();
        final String password = authentication.getCredentials().toString();
        final Collection<SimpleAuthentication> users = idpConfiguration.getUsers();
        for (SimpleAuthentication user : users) {
            if (user.getPrincipal().equals(name) && user.getCredentials().equals(password)) {
                return user;
            }
        }
        throw new AuthenticationException("Can not log in") {};
    }

    @Override
    public boolean supports(final Class method) {
        return method.equals(UsernamePasswordAuthenticationToken.class);
    }
}
