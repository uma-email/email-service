package org.acme.emailservice.rest;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.qute.api.ResourcePath;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.acme.emailservice.model.OidcConfig;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Path("/graphiql")
@ApplicationScoped
public class GraphiqlResource {

    @ConfigProperty(name = "OIDC_AUTH_SERVER_URL")
    String oidcAuthServerUrl;

    @ConfigProperty(name = "OIDC_AUTH_SERVER_REALM")
    String oidcAuthServerRealm;

    @ConfigProperty(name = "OIDC_CREDENTIALS_CLIENT_ID_FRONTEND")
    String oidcCredentialsClientIdFrontend;

    @Inject
    @ResourcePath("graphiql/index.html")
    Template index;
    
    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance index() {
        OidcConfig oidcConfig = new OidcConfig();
        oidcConfig.oidcAuthServerUrl = oidcAuthServerUrl;
        oidcConfig.oidcAuthServerRealm = oidcAuthServerRealm;
        oidcConfig.oidcCredentialsClientIdFrontend = oidcCredentialsClientIdFrontend;
        return index.data("oidcConfig", oidcConfig);
    } 
}
