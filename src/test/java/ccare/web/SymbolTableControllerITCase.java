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

import ccare.domain.SpaceSummary;
import ccare.domain.TableReference;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;

/**
 * Created by IntelliJ IDEA.
 * User: carecx
 * Date: 03-Nov-2010
 * Time: 23:14:11
 * To change this template use File | Settings | File Templates.
 */
public class SymbolTableControllerITCase extends IntegrationSupport {

    @Test
    public void testGetSpaces() throws Exception {
        WebResource resource = resource("spaces");
        List<TableReference> spaces = resource.get(new GenericType<List<TableReference>>() {});
        assertNotNull(spaces);
    }
    
    @Test
    public void testCreateViaPost() throws Exception {
        final WebResource resource = resource("spaces");
        List<TableReference> spaces = resource.get(new GenericType<List<TableReference>>() {});
        int size = spaces.size();
        resource.post();
        spaces = resource.get(new GenericType<List<TableReference>>() {});
        assertEquals(size + 1, spaces.size());
        resource.post();
        resource.post();
        resource.post();
        resource.post();
        resource.post();
        spaces = resource.get(new GenericType<List<TableReference>>() {});
        assertEquals(size + 6, spaces.size());     
    }

    @Test
    public void testCreateViaPut() throws Exception {
        final WebResource resource = resource("spaces");
        List<TableReference> spaces = resource.get(new GenericType<List<TableReference>>() {});
        int size = spaces.size();
        resource.path("abc").put();
        spaces = resource.get(new GenericType<List<TableReference>>() {});
        assertEquals(size + 1, spaces.size());
    }
}
