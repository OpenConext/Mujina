package mujina.idp;

import lombok.Getter;
import lombok.Setter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Getter
@Setter
public class FederatedUserAuthenticationToken extends UsernamePasswordAuthenticationToken {

    private Map<String, List<String>> attributes = new TreeMap<>();

    public FederatedUserAuthenticationToken(Object principal, Object credentials, Collection<? extends GrantedAuthority> authorities) {
        super(principal, credentials, authorities);
    }

    public FederatedUserAuthenticationToken clone() {
        FederatedUserAuthenticationToken clone = new FederatedUserAuthenticationToken(getPrincipal(), getCredentials(), getAuthorities());
        clone.setAttributes(attributes);
        return clone;
    }
}
