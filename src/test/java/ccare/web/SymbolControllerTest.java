package ccare.web;

import ccare.domain.Observable;
import ccare.domain.SecurityDescriptor;
import com.sun.jersey.test.framework.JerseyTest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * User: carecx
 * Date: 13-Oct-2010
 * Time: 12:59:36
 */
public class SymbolControllerTest extends JerseyTest {

    public SymbolControllerTest() throws Exception {
        super("ccare.web");
    }

    @Test
    public void testGetSummary() throws Exception {
        String responseMsg = webResource.path("symbols").get(String.class);
        assertEquals("All symbols served from here", responseMsg);
    }

    @Test
    public void testGetSymbolValue() throws Exception {
        String resp = webResource.path("symbols/a").get(String.class);
        assertEquals("eval(B + C)", resp);
    }

    @Test
    public void testNestedSymbolValue() throws Exception {
        String resp = webResource.path("symbols/a/b").get(String.class);
        assertEquals("eval(B + C)", resp);
    }

    @Test
    public void testNestedSymbolValueBySelector() throws Exception {
        String resp = webResource.path("symbols/a/b:value").get(String.class);
        assertEquals("eval(B + C)", resp);
    }

    @Test
    public void testGetSymbolDefn() throws Exception {
        Observable resp = webResource.path("symbols/a:definition").get(Observable.class);
        assertEquals("B + C", resp.getDefinition());
    }

    @Test
    public void testGetSymbolAccessInfo() throws Exception {
        SecurityDescriptor access = webResource.path("symbols/a:access").get(SecurityDescriptor.class);
        assertEquals(SecurityDescriptor.ALLOW_ALL, access);
    }

    @Test
    public void testGetHeadInfo() throws Exception {
        String resp = webResource.path("symbols/a").options(String.class);
        System.out.println(resp);
    }


}
