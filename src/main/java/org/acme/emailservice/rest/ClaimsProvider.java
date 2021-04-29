package org.acme.emailservice.rest;

// import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import org.acme.emailservice.security.Claims;
import org.acme.emailservice.security.ClaimsConfig;
import org.acme.emailservice.security.ClaimsService;
import org.jose4j.jwk.JsonWebKeySet;

@Path("claims")
public class ClaimsProvider {

    @Inject
    ClaimsService claimsService;

    @Inject
    ClaimsConfig claimsConfig;

    @Context
    UriInfo ui;

    @GET
    @Produces("application/json")
    @Path(".well-known/claims-configuration")
    public String discovery() {
        final String baseUri = ui.getBaseUriBuilder().path("claims").build().toString();
        return "{" +
                "   \"claims_endpoint\":" + "\"" + baseUri + "/token\"," +
                "   \"jwks_uri\":" + "\"" + baseUri + "/jwks\"" +
                "  }";
    }

    @GET
    @Produces("application/json")
    @Path("jwks")
    public String jwks() {
        String json = new JsonWebKeySet(claimsConfig.getKey()).toJson();
        return json;
    }

    @POST
    @Path("token")
    // @RolesAllowed("rp_agent")
    @Produces("application/json")
    public String token(Claims claims) {
        return "{\"claims_token\": \"" + claimsService.generateToken(claims) + "\"," +
                "   \"expires_in\": 300 }";
    }
    
}
