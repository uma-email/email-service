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

import org.acme.emailservice.model.Account;
import org.acme.emailservice.model.AccountInit;
import org.acme.emailservice.model.GoogleCredentials;
import org.acme.emailservice.model.User;
import org.acme.emailservice.service.AccountService;
import org.acme.emailservice.service.UserService;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.Claim;
import org.eclipse.microprofile.jwt.Claims;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.logging.Logger;

import io.quarkus.security.Authenticated;
import io.quarkus.security.identity.SecurityIdentity;

@Path("/user")
@RequestScoped
public class UserProfileApi {

    private static Logger log = Logger.getLogger(UserProfileApi.class);

    @Inject
    SecurityIdentity identity;

    @Inject
    JsonWebToken jwt;

    @Inject @Claim(standard = Claims.given_name)
    String givenName;

    @Inject @Claim(standard = Claims.email)
    String emailAddress;

    @Inject
    UserService userService;

    @Inject
    AccountService accountService;

    @GET
    @Path("profile")
    @Produces(MediaType.TEXT_PLAIN)
    @Authenticated
    public String helloAuthenticated() {
        
        User user = userService.findOrCreate(identity.getPrincipal().getName());

        Account account = accountService.findOrCreate(user, emailAddress);

        log.info(account.getUser().getId() + " / " + account.getEmailAddress());

        return givenName;
    }
}
