package nl.surfnet.mockoleth.model;

import java.util.Collection;
import java.util.Map;

public interface IdpConfiguration extends CommonConfiguration {
    Map<String, String> getAttributes();

    Collection<CustomAuthentication> getUsers();

}
