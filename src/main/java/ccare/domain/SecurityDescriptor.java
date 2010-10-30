/*
 * Copyright (c) 2010, Charles Care
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * * Neither the name of the author nor the names of its contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

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
