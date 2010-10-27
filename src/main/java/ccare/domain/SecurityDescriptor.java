package ccare.domain;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * User: carecx
 * Date: 13-Oct-2010
 * Time: 15:50:38
 */
@XmlRootElement
public class SecurityDescriptor implements Serializable {

    @XmlElement
    private Collection<String> execute;
    @XmlElement
    private Collection<String> write;
    @XmlElement
    private Collection<String> read;


    public static final SecurityDescriptor ALLOW_ALL
            = new SecurityDescriptor(Arrays.asList(new String[]{"ALL"}),
            Arrays.asList(new String[]{"ALL"}),
            Arrays.asList(new String[]{"ALL"}));

    public SecurityDescriptor(List<String> execute, List<String> write, List<String> read) {
        this.execute = execute;
        this.write = write;
        this.read = read;
    }

    public SecurityDescriptor() {
    }

    @Override
    public String toString() {
        return "SecurityDescriptor{" +
                "execute=" + (execute == null ? null : execute) +
                ", write=" + (write == null ? null : write) +
                ", read=" + (read == null ? null : read) +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SecurityDescriptor that = (SecurityDescriptor) o;

        if (execute != null ? !execute.equals(that.execute) : that.execute != null) return false;
        if (read != null ? !read.equals(that.read) : that.read != null) return false;
        if (write != null ? !write.equals(that.write) : that.write != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = execute != null ? execute.hashCode() : 0;
        result = 31 * result + (write != null ? write.hashCode() : 0);
        result = 31 * result + (read != null ? read.hashCode() : 0);
        return result;
    }
}
