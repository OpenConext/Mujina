package nl.surfnet.mockoleth.model;

import java.util.Collection;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

public class SimpleAuthentication implements Authentication {

    private String username;
    private String password;
    private Collection<GrantedAuthority> authorities;


    public SimpleAuthentication(String username,
                                String password,
                                Collection<GrantedAuthority> authorities) {
        this.username = username;
        this.password = password;
        this.authorities = authorities;
    }

    @Override
    public Collection<GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public Object getCredentials() {
        return password;
    }

    @Override
    public Object getDetails() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return username;
    }

    @Override
    public boolean isAuthenticated() {
        return true;
    }

    @Override
    public void setAuthenticated(final boolean isAuthenticated) throws IllegalArgumentException {
    }

    @Override
    public String toString() {
        return username + " / " + password;
    }

    @Override
    public String getName() {
        return username;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final SimpleAuthentication that = (SimpleAuthentication) o;

        if (authorities != null ? !authorities.equals(that.authorities) : that.authorities != null) {
            return false;
        }
        if (password != null ? !password.equals(that.password) : that.password != null) {
            return false;
        }
        if (username != null ? !username.equals(that.username) : that.username != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = username != null ? username.hashCode() : 0;
        result = 31 * result + (password != null ? password.hashCode() : 0);
        result = 31 * result + (authorities != null ? authorities.hashCode() : 0);
        return result;
    }
}
