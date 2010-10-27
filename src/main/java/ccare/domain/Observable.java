package ccare.domain;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;

/**
 * User: carecx
 * Date: 13-Oct-2010
 * Time: 12:16:29
 */

@XmlRootElement
public class Observable implements Serializable {

    private String definition;

    private Object currentValue;

    @XmlElement
    private Collection<Observable> dependents = new HashSet<Observable>();

    @XmlElement
    private Collection<Observable> dependees = new HashSet<Observable>();

    public Object getCurrentValue() {
        return currentValue;
    }

    @XmlElement
    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
        this.currentValue = "eval(" + definition + ")";
    }

    public Collection<Observable> getDependees() {
        return dependees;
    }

    public Collection<Observable> getDependents() {
        return dependents;
    }

    public void setCurrentValue(Object o) {
        currentValue = o;
    }
}
