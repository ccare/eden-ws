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
import ccare.service.CannotCreateException;
import ccare.service.SymbolTableBean;
import ccare.symboltable.Symbol;
import ccare.symboltable.SymbolReference;
import ccare.symboltable.SymbolTable;
import com.sun.jersey.api.*;
import com.sun.jersey.api.core.InjectParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static java.lang.String.format;

@Path("spaces")
public class SymbolTableController {

    private static final Logger logger = LoggerFactory.getLogger(SymbolTableController.class);
    
    @InjectParam("service")
    SymbolTableBean service;

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public List<TableReference> allSpaces(@QueryParam("evaluate") List<String> evals) {
        if (evals == null || evals.isEmpty()) {
            logger.debug(format("Get all spaces"));   
            final List<TableReference> all = service.allSpaces();
            return all;
        } else {
            throw new RuntimeException("ouch");
        }
    }

    @POST
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public TableReference submitSpace(final TableReference reference) {
        logger.debug(format("Recieved POST space request for %s", reference.getName()));
        try {
            return service.createSpace(reference);
        } catch (CannotCreateException e) {
            throw new WebApplicationException(400);
        }
    }

    @GET
    @Path("{spaceName: [^/]+}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Object getSpace(final @PathParam("spaceName") String spaceName, @QueryParam("evaluate") List<String> evals) {
        if (evals == null || evals.isEmpty()) {
            logger.debug(format("Received GET space request for %s", spaceName));
            return service.getSpaceSummary(spaceName);
        } else {
            return service.getSpace(spaceName).evaluate(evals.get(0)).toString();
        }
    }

    @PUT
    @Path("{spaceName: [^/]+}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public TableReference createSpace(final @PathParam("spaceName") String spaceName) {
        logger.debug(format("Received PUT space request for %s", spaceName));
        try {
            return service.createSpace(spaceName);
        } catch (CannotCreateException e) {
            throw new WebApplicationException(400);
        }
    }

    @DELETE
    @Path("{spaceName: [^/]+}")
    public void deleteSpace(final @PathParam("spaceName") String spaceName) {
        logger.debug(format("Deleting space request for %s", spaceName));
        service.deleteSpace(spaceName);
    }

    @GET
    @Path("{spaceName: [^/]+}/{symbolName: [^/]+}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Object observeSymbol(final @PathParam("spaceName") String spaceName,
                                        final @PathParam("symbolName") String symbolName) {
        logger.debug(format("Received GET (observe symbol) for %s.%s", spaceName, symbolName));
        return doGetValue(spaceName, symbolName);
    }
    
    @POST
    @Path("{spaceName: [^/]+}/{symbolName: [^/]+}")
    @Consumes("application/x-www-form-urlencoded")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Object executeSymbolForPostData(final @PathParam("spaceName") String spaceName,
                                        final @PathParam("symbolName") String symbolName,
                                        final @Context UriInfo ui) {
        logger.debug(format("Received POST with post data (execute) for %s.%s", spaceName, symbolName));
        MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
        return doPostValue(spaceName, symbolName, extractExecParams(queryParams));
    }
    
    @POST
    @Path("{spaceName: [^/]+}/{symbolName: [^/]+}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Object executeSymbol(final @PathParam("spaceName") String spaceName,
                                        final @PathParam("symbolName") String symbolName) {
        logger.debug(format("Received POST (execute) for %s.%s", spaceName, symbolName));
        return doPostValue(spaceName, symbolName);
    }
    
    private Object[] extractExecParams(MultivaluedMap<String, String> map) {
    	Object[] params = new Object[map.size()];
    	int i = 0;
    	for (String p : map.keySet() ) {
    		params[i] = map.getFirst(p);
    		i++;
    	}
    	return params;
    }
    
    


//    @GET
//    @Path("{spaceName: [^/]+}/{symbolName: [^:/]+}:value")
//    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
//    public Object observeSymbolValue(final @PathParam("spaceName") String spaceName,
//                                        final @PathParam("symbolName") String symbolName) {
//        logger.debug(format("Received GET (observe symbol value) for %s.%s", spaceName, symbolName));
//        return doGetValue(spaceName, symbolName);
//    }

    private Object doGetValue(String spaceName, String symbolName) {
        final SymbolTable table = service.getSpace(spaceName);
        if (table == null) {
            throw new NotFoundException();
        }
        final SymbolReference ref = new SymbolReference(symbolName);
        if (table.listSymbols().contains(ref)) {
            return table.getValue(ref).toString();
        } else {
            throw new NotFoundException();
        }
    }
    
    private Object doPostValue(String spaceName, String symbolName, Object... params) {
        final SymbolTable table = service.getSpace(spaceName);
        if (table == null) {
            throw new NotFoundException();
        }
        final SymbolReference ref = new SymbolReference(symbolName);
        if (table.listSymbols().contains(ref)) {
            return table.execute(ref);
        } else {
            throw new NotFoundException();
        }
    }

    @PUT
    @Path("{spaceName: [^/]+}/{symbolName: [^/]+}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public void defineSymbol(final @PathParam("spaceName") String spaceName,
                                        final @PathParam("symbolName") String symbolName,
                                        final String definition) {
        logger.debug(format("Received PUT space request for %s.%s", spaceName, symbolName));
        final SymbolTable table = service.getSpace(spaceName);
        if (table == null) {
            throw new NotFoundException();
        }
        table.define(new SymbolReference(symbolName), definition.toString());
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