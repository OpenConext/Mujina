package nl.surfnet.mockoleth.model;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class User implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private String name;
    private String password;
    private List<String> authorities;

    public String getName() {
        return name;
    }

    @XmlElement
    public void setName(final String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    @XmlElement
    public void setPassword(final String password) {
        this.password = password;
    }

    public List<String> getAuthorities() {
        return authorities;
    }

    @XmlElement
    public void setAuthorities(final List<String> authorities) {
        this.authorities = authorities;
    }
}
