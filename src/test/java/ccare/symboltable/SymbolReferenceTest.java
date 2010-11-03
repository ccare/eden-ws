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

package ccare.symboltable;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

/**
 * User: carecx
 * Date: 27-Oct-2010
 * Time: 20:50:19
 */
public class SymbolReferenceTest {

    @Test
    public void testGetName() {
        SymbolReference sr = new SymbolReference("foo");
        assertThat(sr.getName(), is(equalTo("foo")));
    }

    @Test
    public void testGetNameNotNullForDefault() {
        SymbolReference sr = new SymbolReference();
        assertNotNull(sr.getName());
    }

    @Test
    public void testEquals() {
        SymbolReference sr = new SymbolReference("foo");

        assertFalse(sr.equals(null));
        assertFalse(sr.equals(new SymbolReference("foo2")));
        assertFalse(sr.equals(new SymbolReference()));
        assertFalse(new SymbolReference().equals(new SymbolReference()));

        assertTrue(sr.equals(sr));
        assertTrue(sr.equals(new SymbolReference("foo")));

        SymbolReference extended = new SymbolReference() {
            @Override
            public String getName() {
                return null;
            }
        };

        assertFalse(sr.equals(extended));
        assertFalse(extended.equals(sr));
    }

    @Test
    public void testToString() {
        SymbolReference sr = new SymbolReference("foo");
        assertThat(sr.toString(), is(equalTo("Ref<foo>")));
    }

    @Test
    public void testHashCode() throws Exception {
        SymbolReference sr = new SymbolReference("foo");
        assertEquals(sr.hashCode(), sr.hashCode());
        assertEquals(sr.hashCode(), new SymbolReference("foo").hashCode());
    }

}
