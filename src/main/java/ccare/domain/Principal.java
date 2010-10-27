package ccare.domain;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * User: carecx
 * Date: 13-Oct-2010
 * Time: 16:00:14
 */

@XmlRootElement
public class Principal implements java.security.Principal, Serializable {
    private String name;

    public static final Principal ALL_USERS = new Principal("EVERYONE");

    public Principal() {
    }

    public Principal(String name) {
        this.name = name;
    }

    @XmlElement
    public String getName() {
        return name;
    }

    public String setName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Principal principal = (Principal) o;
        if (name != null ? !name.equals(principal.name) : principal.name != null) return false;

        return true;
    }

    @Override
    public String toString() {
        return "User: " + getName();
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
}
