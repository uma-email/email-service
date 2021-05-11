package org.acme.emailservice.rest.client;

import javax.enterprise.context.RequestScoped;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.acme.emailservice.security.Claims;
import org.acme.emailservice.security.ClaimsTokenResponse;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Singleton
@RequestScoped
@Path("/claims")
@RegisterRestClient(configKey = "claims-provider")
public interface ClaimsProviderRestClient {

    @POST
    @Path("token")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public ClaimsTokenResponse getClaimsToken(@HeaderParam("Authorization") String auth, Claims claims);
}
