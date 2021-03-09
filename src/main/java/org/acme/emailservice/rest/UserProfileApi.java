package org.acme.emailservice.rest;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.acme.emailservice.model.Account;
import org.acme.emailservice.model.Label;
import org.acme.emailservice.model.User;
import org.acme.emailservice.model.enums.ELabelRole;
import org.acme.emailservice.service.AccountService;
import org.acme.emailservice.service.UserService;
import org.eclipse.microprofile.jwt.JsonWebToken;
// import org.jboss.logging.Logger;

import io.quarkus.oidc.UserInfo;
import io.quarkus.security.Authenticated;
import io.quarkus.security.identity.SecurityIdentity;

@Path("/user")
@RequestScoped
@RolesAllowed({ "user", "admin" })
public class UserProfileApi {

    // private static Logger log = Logger.getLogger(UserProfileApi.class);

    @Inject
    SecurityIdentity identity;

    @Inject
    JsonWebToken jwt;

    @Inject
    UserService userService;

    @Inject
    AccountService accountService;

    @PersistenceContext
    EntityManager em;

    private String getUsername() {
        return identity.getPrincipal().getName();
    }

    private String getEmailAddress() {
        UserInfo userInfo =  identity.getAttribute("userinfo");
        return userInfo.getString("email");
    }

    @GET
    @Path("profile")
    @Produces(MediaType.APPLICATION_JSON)
    @Authenticated
    @Transactional
    public String getAuthenticated() {

        User user = userService.findOrCreateProfile(getUsername(), getEmailAddress());

        Jsonb jsonb = JsonbBuilder.create();
        return jsonb.toJson(user);
    }
}
