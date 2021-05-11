package org.acme.emailservice.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

@Path("/aems")
public class WellKnownProvider {

    @Context
    UriInfo ui;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path(".well-known/aems-configuration")
    public String discovery() {
        final String baseUri = ui.getBaseUriBuilder().path("aems").build().toString();
        return "{" +
                "   \"incoming_resources_endpoint\":" + "\"" + baseUri + "/incoming\"," +
                "   \"outgoing_resources_endpoint\":" + "\"" + baseUri + "/outgoing\"" +
                "  }";
    }
}
