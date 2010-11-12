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
import com.sun.jersey.api.client.WebResource;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static junit.framework.Assert.assertNotNull;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

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
    public void testCreateTableAndSymbol() {
        final String spaceName = "abc";
        final String objectName = "abc/a";
        assertEquals(ClientResponse.Status.OK, resource.head().getClientResponseStatus());
        isNotFound(resource, spaceName);
        isNotFound(resource, objectName);
        resource.path(spaceName).put();
        isOk(resource, spaceName);
        isNotFound(resource, objectName);
        resource.path(objectName).put("12");
        isOk(resource, objectName);
        String o = resource.path(objectName).get(String.class);
        assertEquals("12", o);
    }

    private void isOk(final WebResource resource, String pth) {
        assertEquals(ClientResponse.Status.OK, resource.path(pth).head().getClientResponseStatus());
    }

    private void isNotFound(final WebResource resource, String pth) {
        assertEquals(ClientResponse.Status.NOT_FOUND, resource.path(pth).head().getClientResponseStatus());
    }
}
