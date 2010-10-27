package ccare.web;

import com.sun.jersey.test.framework.JerseyTest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * User: carecx
 * Date: 13-Oct-2010
 * Time: 14:50:23
 */
public class StatusControllerTest extends JerseyTest {

    public StatusControllerTest() throws Exception {
        super("ccare.web");
    }

    @Test
    public void testGetStatus() throws Exception {
        String responseMsg = webResource.path("status").get(String.class);
        assertEquals("Running and fine", responseMsg);
    }
}
