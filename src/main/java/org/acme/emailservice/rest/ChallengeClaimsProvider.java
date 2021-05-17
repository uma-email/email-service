package org.acme.emailservice.rest;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;

import org.acme.emailservice.security.ChallengeClaims;
import org.acme.emailservice.security.ChallengeClaimsProviderConfig;
import org.acme.emailservice.security.ChallengeClaimsProviderService;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.Claim;
import org.eclipse.microprofile.jwt.Claims;
import org.jose4j.jwk.JsonWebKeySet;

@Path("/claims")
public class ChallengeClaimsProvider {

    @Inject
    ChallengeClaimsProviderService claimsService;

    @Inject
    ChallengeClaimsProviderConfig claimsConfig;

    @Context
    UriInfo ui;

    @Inject
    @Claim(standard = Claims.sub)
    String subject;

    @ConfigProperty(name = "uma.wide-ecosystem.well-known.claims-provider")
    String wellKnownClaimsProvider;

    @ConfigProperty(name = "uma.wide-ecosystem.challenge-client-id")
    String challengeClientId;

    public class WellKnownUmaChallenge {
        public String claimsEndpoint;
        public String jwksUri;

        public WellKnownUmaChallenge(String claimsEndpoint, String jwksUri) {
            this.claimsEndpoint = claimsEndpoint;
            this.jwksUri = jwksUri;
        }
    }

    public class ChallengeClaimsToken {
        public String claimsToken;
        public String expiresIn;

        public ChallengeClaimsToken(String claimsToken, String expiresIn) {
            this.claimsToken = claimsToken;
            this.expiresIn = expiresIn;
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{wellKnown}")
    public Response discovery(@PathParam("wellKnown") String wellKnown) {
        if (wellKnownClaimsProvider.equals(wellKnown)) {
            final String baseUri = ui.getBaseUriBuilder().path("claims").build().toString();

            String tokenUri  = baseUri + "/token";
            String jwksUri  = baseUri + "/jwks";
    
            WellKnownUmaChallenge wellKnownUmaChallenge = new WellKnownUmaChallenge(tokenUri, jwksUri);
            return Response.status(Status.OK).entity(wellKnownUmaChallenge).build();
        }

        return Response.status(Status.NOT_FOUND).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("jwks")
    public String jwks() {
        String json = new JsonWebKeySet(claimsConfig.getKey()).toJson();
        return json;
    }

    @POST
    @Path("token")
    @RolesAllowed("rp_agent")
    @Produces(MediaType.APPLICATION_JSON)
    public Response token(ChallengeClaims challengeClaims) {
        if (subject.equals(challengeClientId)) {

            ChallengeClaimsToken challengeClaimsToken = new ChallengeClaimsToken(
                    claimsService.generateToken(challengeClaims), "300");

            return Response.status(Status.OK).entity(challengeClaimsToken).build();
        }
       
       return Response.status(Status.FORBIDDEN).build();
    }
}
