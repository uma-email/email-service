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
        public String incoming_resources_endpoint;
        public String outgoing_resources_endpoint;

        public WellKnownUmaWideConfiguration(String incomingResourcesEndpoint, String outgoingResourcesEndpoint) {
            this.incoming_resources_endpoint = incomingResourcesEndpoint;
            this.outgoing_resources_endpoint = outgoingResourcesEndpoint;
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/.well-known/{configuration}")
    public Response discovery(@PathParam("configuration") String configuration) {
        if (wellKnownConfiguration.equals("/.well-known/" + configuration)) {
            final String baseUri = ui.getBaseUriBuilder().path("rs").build().toString();

            String incomingResourcesEndpoint  = baseUri + "/incoming";
            String outgoingResourcesEndpoint  = baseUri + "/outgoing";

            WellKnownUmaWideConfiguration wellKnownUmaWideConfiguration = new WellKnownUmaWideConfiguration(incomingResourcesEndpoint, outgoingResourcesEndpoint);
            return Response.status(Status.OK).entity(wellKnownUmaWideConfiguration).build();
        }
        
        return Response.status(Status.NOT_FOUND).build();
    }
}
