package nl.surfnet.mockoleth.spring.security;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import nl.surfnet.mockoleth.model.IdpConfiguration;

public class CustomAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    private IdpConfiguration idpConfiguration;
    
    @Override
    public Authentication authenticate(final Authentication authentication) throws AuthenticationException {
        final String name = authentication.getName();
        final String password = authentication.getCredentials().toString();
        final Collection<CustomAuthentication> users = idpConfiguration.getUsers();
        for (CustomAuthentication customAuthentication : users) {
            CustomAuthentication.User user = (CustomAuthentication.User)customAuthentication.getCredentials();
            if (user.getUsername().equals(name) && user.getPassword().equals(password)) {
                return customAuthentication;
            }
        }
        return null;
    }

    @Override
    public boolean supports(final Class method) {
        return method.equals(UsernamePasswordAuthenticationToken.class);
    }
}
