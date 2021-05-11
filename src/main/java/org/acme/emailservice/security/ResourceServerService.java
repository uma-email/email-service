package org.acme.emailservice.security;

import javax.enterprise.context.ApplicationScoped;

import org.keycloak.authorization.client.AuthzClient;
import org.keycloak.representations.idm.authorization.PermissionRequest;
import org.keycloak.representations.idm.authorization.PermissionResponse;

@ApplicationScoped
public class ResourceServerService {

    AuthzClient rsAuthzClient = AuthzClient
            .create(Thread.currentThread().getContextClassLoader().getResourceAsStream("keycloak-rs-service.json"));

    public static final String SCOPE_MESSAGE_CREATE = "message:create";
    public static final String SCOPE_MESSAGE_VIEW = "message:view";
    public static final String SCOPE_MESSAGE_DELETE = "message:delete";

    public String getIncomingBoxId() {
        return rsAuthzClient.protection().resource().findByName("Incoming Box").getId();
    }

    public String getOutgoingBoxId() {
        return rsAuthzClient.protection().resource().findByName("Outgoing Box").getId();
    }

    public String getTicket(String incomingBoxId) {
        // create permission request
        PermissionRequest permissionRequest = new PermissionRequest(incomingBoxId);
        permissionRequest.addScope(SCOPE_MESSAGE_CREATE);

        PermissionResponse pmResponse = rsAuthzClient.protection().permission().create(permissionRequest);
        return pmResponse.getTicket();
    }

}
