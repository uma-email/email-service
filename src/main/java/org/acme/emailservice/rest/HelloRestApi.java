package org.acme.emailservice.rest;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.acme.emailservice.model.AccountInit;
import org.acme.emailservice.model.GoogleCredentials;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.Claim;
import org.eclipse.microprofile.jwt.Claims;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.logging.Logger;

import io.quarkus.security.Authenticated;
import io.quarkus.security.identity.SecurityIdentity;

@Path("/hello")
@RequestScoped
public class HelloRestApi {

    private static Logger log = Logger.getLogger(HelloRestApi.class);

    @ConfigProperty(name = "GOOGLE_CREDENTIALS_FAKE")
    String googleCredentialsConfig;

    @ConfigProperty(name = "ACCOUNTS_INIT_FAKE")
    String accountsInitConfig;

    @Inject
    SecurityIdentity identity;

    @Inject
    JsonWebToken jwt;

    @Inject @Claim(standard = Claims.given_name)
    String givenName;

    @GET
    @Path("credentials")
    @PermitAll
    @Produces(MediaType.APPLICATION_JSON)
    public String helloCredentials() {
        Jsonb jsonb = JsonbBuilder.create();
        GoogleCredentials googleCredentials = jsonb.fromJson(googleCredentialsConfig, GoogleCredentials.class);
        log.debug(jsonb.toJson(googleCredentials));
        return jsonb.toJson(googleCredentials);
    }
    
    @GET
    @Path("accounts")
    @PermitAll
    @Produces(MediaType.APPLICATION_JSON)
    public String helloAccounts() {
        Jsonb jsonb = JsonbBuilder.create();
        AccountInit[] accountInitArray = jsonb.fromJson(accountsInitConfig, new AccountInit[] {}.getClass());
        log.debug(jsonb.toJson(accountInitArray));
        return jsonb.toJson(accountInitArray);
    }
    
    @GET
    @Path("public")
    @PermitAll
    @Produces(MediaType.TEXT_PLAIN)
    public String helloPublic() {
        return "public";
    }

    @GET
    @Path("private")
    @Produces(MediaType.TEXT_PLAIN)
    @RolesAllowed({ "user", "admin" })
    public String helloPrivate() {
        return "private";
    }

    @GET
    @Path("private-admin")
    @Produces(MediaType.TEXT_PLAIN)
    @RolesAllowed("admin")
    public String helloPrivateAdmin() {
        return "private-admin";
    }

    @GET
    @Path("authenticated")
    @Produces(MediaType.TEXT_PLAIN)
    @Authenticated
    public String helloAuthenticated() {
        //return "authenticated";
        //return identity.getPrincipal().getName();
        //return jwt.getClaim("given_name");
        return givenName;
    }

}