package org.acme.emailservice.security;

import javax.enterprise.context.ApplicationScoped;

import org.acme.emailservice.model.enums.EResourceType;
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

    public String getTicket(EResourceType resourceType) {
        PermissionRequest permissionRequest;

        // create permission request
        switch (resourceType) {
            case INCOMING:
                permissionRequest = new PermissionRequest(getIncomingBoxId());
                permissionRequest.addScope(SCOPE_MESSAGE_CREATE);
                break;
            case OUTGOING:
                permissionRequest = new PermissionRequest(getOutgoingBoxId());
                permissionRequest.addScope(SCOPE_MESSAGE_VIEW);
                break;
            default:
                return null;
        }

        PermissionResponse pmResponse = rsAuthzClient.protection().permission().create(permissionRequest);
        return pmResponse.getTicket();
    }

}
