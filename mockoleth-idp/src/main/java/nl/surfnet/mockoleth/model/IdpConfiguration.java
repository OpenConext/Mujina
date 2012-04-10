package nl.surfnet.mockoleth.model;

import java.util.Collection;
import java.util.Map;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

public interface IdpConfiguration extends CommonConfiguration {
    Map<String, String> getAttributes();

    Collection<UsernamePasswordAuthenticationToken> getUsers();

}
