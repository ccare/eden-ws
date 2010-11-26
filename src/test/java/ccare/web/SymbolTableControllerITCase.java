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

package ccare.web;

import ccare.domain.TableReference;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static junit.framework.Assert.assertNotNull;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * Created by IntelliJ IDEA.
 * User: carecx
 * Date: 03-Nov-2010
 * Time: 23:14:11
 * To change this template use File | Settings | File Templates.
 */
public class SymbolTableControllerITCase extends IntegrationSupport {
    private static final GenericType<List<TableReference>> TABLE_REF_COLLECTION_TYPE = new GenericType<List<TableReference>>() {};
    private final WebResource resource = resource("spaces");

    @Test
    public void testGetSpaces() throws Exception {
        Collection<TableReference> spaces = resource.get(TABLE_REF_COLLECTION_TYPE);
        assertNotNull(spaces);
    }

    @Test
    public void testCreateViaPost() throws Exception {
        int size = spaceCount(resource);
        final String newName = "myname";
        TableReference newRef = resource.post(TableReference.class, new TableReference(newName));
        assertEquals(newName, newRef.getName());
        assertNotNull(newRef.getId());
        Collection<TableReference> spaces = resource.get(TABLE_REF_COLLECTION_TYPE);
        assertEquals(size + 1, spaces.size());
        containsName(spaces, newName);
    }

    private int spaceCount(WebResource resource) {
        Collection<TableReference> spaces = resource.get(TABLE_REF_COLLECTION_TYPE);
        int size = spaces.size();
        return size;
    }

    @Test
    public void testCreateViaPut() throws Exception {
        int size = spaceCount(resource);
        final String newName = "abc";
        resource.path(newName).put();
        Collection<TableReference> spaces = resource.get(TABLE_REF_COLLECTION_TYPE);
        assertEquals(size + 1, spaces.size());
        containsName(spaces, newName);
    }

    @Test
    public void testCreateMultipleTimesFails() throws Exception {
        int size = spaceCount(resource);
        final String newName = "duplicateSpace" + Math.random();
        resource.path(newName).put();
        try {
            resource.path(newName).put();
            fail("Expected an exception to be thrown");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getClientResponseStatus().getStatusCode(), is(equalTo(400)));
        } 
    }

    private void containsName(Collection<TableReference> spaces, String newName) {
        final List<String> allNames = new ArrayList<String>(spaces.size());
        for (TableReference ref : spaces) {
            allNames.add(ref.getName());
        }
        assertThat(allNames, hasItem(newName));
    }

    @Test
    public void testDeleteSpace() throws Exception {
        Collection<TableReference> spaces = resource.get(TABLE_REF_COLLECTION_TYPE);
        int size = spaces.size();
        resource.path("abc").delete();
        spaces = resource.get(TABLE_REF_COLLECTION_TYPE);
        assertEquals(size - 1, spaces.size());        
    }

    @Test
    public void testCreateTableAndSymbolsWithDependencies() {
        // Paths
        final String spaceName = "abc";
        final String refA = "abc/a";
        final String refB = "abc/b";
        final String refC = "abc/c";
        // Check service is ok
        assertEquals(ClientResponse.Status.OK, resource.head().getClientResponseStatus());
        // Assert that we start with no space, and that a symbol with that space returns 404
        isNotFound(resource, spaceName);
        isNotFound(resource, refA);
        // Create the space and assert it's there, check that the symbol inside still returns 404
        resource.path(spaceName).put();
        isOk(resource, spaceName);
        isNotFound(resource, refA);
        // Define the symbol
        resource.path(refA).put("12");
        isOk(resource, refA);
        // Get the value and assert ok
        String o = resource.path(refA).get(String.class);
        assertEquals("12", o);

        // Create dependent definitions and assert values
        resource.path(refB).put("#a");
        assertEquals("12", resource.path(refB).get(String.class));
        resource.path(refB).put("#a + 8");
        assertEquals("20.0", resource.path(refB).get(String.class));
        resource.path(refC).put("#b + 'aaa'");
        assertEquals("20aaa", resource.path(refC).get(String.class));

        // Change a source observable, and check dependent is updated
        resource.path(refA).put("2");
        assertEquals("10aaa", resource.path(refC).get(String.class));
    }

    @Test
    public void testEvaluate() {
        resource.path("foo").put();
        assertEquals("13", resource.path("foo").queryParam("evaluate","14 - 1").get(String.class));
        resource.path("foo/a").put("2");
        assertEquals("1.0", resource.path("foo").queryParam("evaluate","#a - 1").get(String.class));
        resource.path("foo/a").put("3");
        assertEquals("2.0", resource.path("foo").queryParam("evaluate","#a - 1").get(String.class));
    }


    @Test
    public void testExecuteFunction() {
        // Paths
        final String spaceName = "execSpace";
        final String refFn = spaceName + "/fn";
        final String refFn2 = spaceName + "/fn2";
        resource.path(spaceName).put();
        resource.path(refFn).put("function() { return 'hi'; }");
        assertEquals("hi", resource.path(spaceName).queryParam("evaluate","#fn()").get(String.class));
        resource.path(refFn).put("function() { return 2*2; }");
        assertEquals("4", resource.path(spaceName).queryParam("evaluate","#fn()").get(String.class));
        resource.path(refFn2).put("function() { return 'foo ' + #fn() }");
        assertEquals("foo 4", resource.path(spaceName).queryParam("evaluate","#fn2()").get(String.class));
        resource.path(spaceName).delete();


    }

    @Test
    public void testDependOnFunction() {
        // Paths
        final String spaceName = "fnSpace";
        resource.path(spaceName).put();
        try {
            final String refA = spaceName + "/A";
            final String refB = spaceName + "/B";
            final String refFn = spaceName + "/fn";
            resource.path(refFn).put("function(a) { return a + 1; }");
            resource.path(refA).put("#fn(#B)");
            resource.path(refB).put("20000");
            assertEquals("20001.0", resource.path(refA).get(String.class));
        } finally {
            resource.path(spaceName).delete();
        }
    }

    @Test
    public void testCallFunctionWithPost() {
        // Paths
        final String spaceName = "fnSpace2";
        resource.path(spaceName).put();
        try {
            final String refHello = spaceName + "/hello";
            resource.path(refHello).put("function() { return 'hello world' }");
            assertEquals("hello world", resource.path(refHello).post(String.class));
            assertEquals("hello world", resource.path(refHello).post(String.class, "hi"));
        } finally {
            resource.path(spaceName).delete();
        }
    }

    @Ignore
    @Test
    public void testCallFunctionWithPostBody() {
        // Paths
        final String spaceName = "fnSpace2";
        resource.path(spaceName).put();
        try {
            final String refHello = spaceName + "/hello";
            resource.path(refHello).put("function(param) { return param + ' world' }");
            assertEquals("hello world", resource.path(refHello).post(String.class, "hi"));
        } finally {
            resource.path(spaceName).delete();
        }
    }



    private void isOk(final WebResource resource, String pth) {
        assertEquals(ClientResponse.Status.OK, resource.path(pth).head().getClientResponseStatus());
    }

    private void isNotFound(final WebResource resource, String pth) {
        assertEquals(ClientResponse.Status.NOT_FOUND, resource.path(pth).head().getClientResponseStatus());
    }
}
