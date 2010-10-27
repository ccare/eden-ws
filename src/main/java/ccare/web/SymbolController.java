package ccare.web;

import ccare.domain.Observable;
import ccare.domain.SecurityDescriptor;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

@Path("symbols")
public class SymbolController {

    @GET
    @Produces("text/plain")
    public String getSummary() {
        return "All symbols served from here";
    }

    @GET
    @Produces("application/xml")
    @Path("{symbolName: [^:]+[:value]*}")
    public Object getSymbolValue(@PathParam("symbolName") String symbolName) {
        Observable o = new Observable();
        o.setDefinition("B + C");
        return o.getCurrentValue();
    }

    @GET
    @Produces("application/xml")
    @Path("{symbolName}:definition")
    public Observable getSymbolDefn(@PathParam("symbolName") String symbolName) {
        Observable o = new Observable();
        o.setDefinition("B + C");
        return o;
    }

    @GET
    @Produces("application/xml")
    @Path("{symbolName}:access")
    public SecurityDescriptor getSecurityDescriptor(@PathParam("symbolName") String symbolName) {
        return SecurityDescriptor.ALLOW_ALL;
    }


}