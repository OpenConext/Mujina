package nl.surfnet.mockoleth.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Attribute implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private String value;

    public String getName() {
        return name;
    }

    @XmlElement
    public void setName(final String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    @XmlElement
    public void setValue(final String value) {
        this.value = value;
    }
}
