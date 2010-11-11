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

import ccare.domain.Observable;
import ccare.domain.SecurityDescriptor;
import ccare.domain.SpaceSummary;
import ccare.domain.SymbolTableRef;
import ccare.service.SymbolTableBean;
import ccare.service.SymbolTableService;
import com.sun.jersey.api.core.InjectParam;

import javax.ejb.EJB;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Path("spaces")
public class SymbolTableController {

    @InjectParam("service")
    SymbolTableBean service;

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public SpaceSummary getSpaces() {
        final SpaceSummary s = new SpaceSummary();           
        return s;
    }

    @POST
    public void createSpace() {
        System.out.println("...");
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