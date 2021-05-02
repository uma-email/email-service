package org.acme.emailservice.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;

import org.keycloak.authorization.client.AuthzClient;
import org.keycloak.common.util.Base64Url;
import org.keycloak.representations.idm.authorization.PermissionRequest;
import org.keycloak.representations.idm.authorization.PermissionResponse;

@ApplicationScoped
public class ResourceServerService {

    AuthzClient rsAuthzClient = AuthzClient
            .create(Thread.currentThread().getContextClassLoader().getResourceAsStream("keycloak-rs-service.json"));

    public static final String SCOPE_MESSAGE_CREATE = "message:create";
    public static final String SCOPE_MESSAGE_VIEW = "message:view";
    public static final String SCOPE_MESSAGE_DELETE = "message:delete";

    public String generateTicketVerifier() throws Exception {
        String randomUUID = UUID.randomUUID().toString();
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        String input = randomUUID;
        byte[] check = md.digest(input.getBytes(StandardCharsets.UTF_8));
        return Base64Url.encode(check); // aka uma ticket - see the keycloak uma protocol
    }

    public String generateTicketChallenge(String ticketVerifier) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        return Base64Url.encode(md.digest(ticketVerifier.getBytes(StandardCharsets.UTF_8)));
    }

    public String getIncomingBoxId() {
        return rsAuthzClient.protection().resource().findByName("Incoming Box").getId();
    }

    public String getTicket(String incomingBoxId, String ticketVerifier) {
        // create permission request
        PermissionRequest permissionRequest = new PermissionRequest(incomingBoxId);
        permissionRequest.addScope(SCOPE_MESSAGE_CREATE);
        permissionRequest.setClaim("ticket_verifier", ticketVerifier);

        // get ticket with ticket verifier - see the keycloak uma protocol
        // implementation differencies
        PermissionResponse pmResponse = rsAuthzClient.protection().permission().create(permissionRequest);
        return pmResponse.getTicket();
    }

}
