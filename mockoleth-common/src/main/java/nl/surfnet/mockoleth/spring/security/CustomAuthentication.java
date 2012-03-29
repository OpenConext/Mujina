package nl.surfnet.mockoleth.spring.security;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;

public class CustomAuthentication implements Authentication {
    private boolean authenticated = true;
    private Collection<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
    private User user;

    public CustomAuthentication(final String username, final String password) {
        this.user = new User(username, password);
    }    

    public void addAuthority(String name) {
        authorities.add(new GrantedAuthorityImpl(name));
    }
    
    @Override
    public Collection<GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public Object getCredentials() {
        return user;
    }

    @Override
    public Object getDetails() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return null;
    }

    @Override
    public boolean isAuthenticated() {
        return authenticated;
    }

    @Override
    public void setAuthenticated(final boolean authenticated) throws IllegalArgumentException {
        this.authenticated = authenticated;
    }

    @Override
    public String getName() {
        return null;
    }
    
    public static class User {
        private String username;
        private String password;
        
        public User(String username, String password) {
            this.username = username;
            this.password = password;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }
    }
}