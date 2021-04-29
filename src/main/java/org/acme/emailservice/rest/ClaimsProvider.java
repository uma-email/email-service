package org.acme.emailservice.rest;

import javax.annotation.PostConstruct;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jose4j.jwk.JsonWebKeySet;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jwk.RsaJwkGenerator;

import io.smallrye.jwt.build.Jwt;

class Claims {

    public String codeChallenge;
    public String emailAddress;

    public Claims(String codeChallenge, String emailAddress) {
        this.codeChallenge = codeChallenge;
        this.emailAddress = emailAddress;
    }
}

@Path("claims")
public class ClaimsProvider {

    @ConfigProperty(name = "mp.jwt.verify.publickey.location")
    String jwtPublicKeyLocation;

    @Context
    UriInfo ui;
    RsaJsonWebKey key;
    
    @PostConstruct
    public void init() throws Exception {
        key = RsaJwkGenerator.generateJwk(2048);
        key.setUse("sig");
        key.setKeyId("1");
        key.setAlgorithm("RS256");
    }

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
        String json = new JsonWebKeySet(key).toJson();
        return json;
    }

    @POST
    @Path("token")
    @Produces("application/json")
    public String token(Claims claims) {
        return "{\"claims_token\": \"" + jwt(claims) + "\"," +
                "   \"expires_in\": 300 }";
    }

    private String jwt(Claims claims) {
        return Jwt.claims()
                .issuer(jwtPublicKeyLocation)
                .claim("code_challenge", claims.codeChallenge)
                .claim("email_address", claims.emailAddress)
                .upn(claims.emailAddress)
                .groups("user")
                .jws().keyId("1")
                .sign(key.getPrivateKey());
    }
    
}
