package org.acme.emailservice.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;

import org.eclipse.microprofile.config.inject.ConfigProperty;

@Path("/uma")
public class WellKnownProvider {

    @Context
    UriInfo ui;

    @ConfigProperty(name = "uma.wide-ecosystem.well-known.configuration")
    String wellKnownConfiguration;

    public class WellKnownUmaWideConfiguration {
        public String incomingResourcesEndpoint;
        public String outgoingResourcesEndpoint;

        public WellKnownUmaWideConfiguration(String incomingResourcesEndpoint, String outgoingResourcesEndpoint) {
            this.incomingResourcesEndpoint = incomingResourcesEndpoint;
            this.outgoingResourcesEndpoint = outgoingResourcesEndpoint;
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{wellKnown}")
    public Response discovery(@PathParam("wellKnown") String wellKnown) {
        if (wellKnownConfiguration.equals(wellKnown)) {
            final String baseUri = ui.getBaseUriBuilder().path("rs").build().toString();

            String incomingResourcesEndpoint  = baseUri + "/incoming";
            String outgoingResourcesEndpoint  = baseUri + "/outgoing";

            WellKnownUmaWideConfiguration wellKnownUmaWideConfiguration = new WellKnownUmaWideConfiguration(incomingResourcesEndpoint, outgoingResourcesEndpoint);
            return Response.status(Status.OK).entity(wellKnownUmaWideConfiguration).build();
        }
        
        return Response.status(Status.NOT_FOUND).build();
    }
}
