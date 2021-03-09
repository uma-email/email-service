package org.acme.emailservice.rest;

import javax.annotation.security.PermitAll;
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
import org.acme.emailservice.model.AccountInit;
import org.acme.emailservice.model.GoogleCredentials;
import org.acme.emailservice.model.Label;
import org.acme.emailservice.model.User;
import org.acme.emailservice.model.enums.ELabelRole;
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

    @PersistenceContext
    EntityManager em;

    @GET
    @Path("profile")
    @Produces(MediaType.APPLICATION_JSON)
    @Authenticated
    @Transactional
    public String getAuthenticated() {

        User user = userService.findOrCreate(identity.getPrincipal().getName());

        for (ELabelRole labelRole : ELabelRole.values()) {
            Label label = new Label();
            label.setUser(user);
            if (labelRole.toString().startsWith("CATEGORY_")) { 
                String labelName = labelRole.toString().substring(("CATEGORY_".length())).toLowerCase();
                label.setName(labelName.substring(0, 1).toUpperCase() + labelName.substring(1));
            } else {
                String labelName = labelRole.toString().toLowerCase();
                label.setName(labelName.substring(0, 1).toUpperCase() + labelName.substring(1));
            }
            label.setRole(labelRole);
            // HistoryId
            label.setHistoryId(Long
                    .parseLong(em.createNativeQuery("select nextval('LABEL_HISTORY_ID')").getSingleResult().toString()));
            // TimelineId
            label.setLastStmt((byte) 0);
            em.persist(label);
            user.addLabel(label);
    
            }

        Account account = accountService.findOrCreate(user, emailAddress);
        user.addAccount(account);

        Jsonb jsonb = JsonbBuilder.create();
        return jsonb.toJson(user);
    }
}
