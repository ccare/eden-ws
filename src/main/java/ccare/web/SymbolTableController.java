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
import ccare.service.SymbolTableBean;
import com.sun.jersey.api.core.InjectParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.UUID;

import static java.lang.String.format;

@Path("spaces")
public class SymbolTableController {

    private static final Logger logger = LoggerFactory.getLogger(SymbolTableController.class);
    
    @InjectParam("service")
    SymbolTableBean service;

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public List<TableReference> getSpaces() {
        logger.debug(format("Get all spaces"));   
        final List<TableReference> all = service.allSpaces();
        return all;
    }

    @PUT
    @Path("{spaceName: [^/]+[/]{0,1}}")
    public void createSpace(final @PathParam("spaceName") String spaceName) {
        logger.debug(format("Creating space request for %s", spaceName));         
        service.createSpace(spaceName);
    }


//    @GET
//    @Produces("application/xml")
//    @Path("{symbolName: [^:]+[:value]*}")
//    public Object getSymbolValue(@PathParam("symbolName") String symbolName) {
//        Observable o = new Observable();
//        o.setDefinition("B + C");
//        return o.getCurrentValue();
//    }
//
//    @GET
//    @Produces("application/xml")
//    @Path("{symbolName}:definition")
//    public Observable getSymbolDefn(@PathParam("symbolName") String symbolName) {
//        Observable o = new Observable();
//        o.setDefinition("B + C");
//        return o;
//    }
//
//    @GET
//    @Produces("application/xml")
//    @Path("{symbolName}:access")
//    public SecurityDescriptor getSecurityDescriptor(@PathParam("symbolName") String symbolName) {
//        return SecurityDescriptor.ALLOW_ALL;
//    }


}