package ccare.domain;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.*;
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
    public void testToString(){
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
