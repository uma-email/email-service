package org.acme.emailservice.security;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.acme.emailservice.rest.client.ClaimsProviderRestClient;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.keycloak.authorization.client.AuthzClient;
import org.keycloak.common.util.Base64;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.authorization.AuthorizationRequest;
import org.keycloak.representations.idm.authorization.AuthorizationResponse;
import org.keycloak.util.JsonSerialization;

@ApplicationScoped
public class RequestingPartyService {

    AuthzClient rpAuthzClient = AuthzClient
            .create(Thread.currentThread().getContextClassLoader().getResourceAsStream("keycloak-rp-agent.json"));

    @Inject
    @RestClient
    ClaimsProviderRestClient claimsProviderRestClient;

    public ClaimsTokenResponse getClaimsToken(String ticketChallenge, String username) {
        // get rp access token
        AccessTokenResponse accessToken = rpAuthzClient.obtainAccessToken();

        // create claims
        Claims claims = new Claims(ticketChallenge, username);
        // get signed claims token from a claims provider
        return claimsProviderRestClient.getClaimsToken("Bearer " + accessToken.getToken(), claims);
    }

    public String getRpt(String ticket, ClaimsTokenResponse claimsTokenResponse) throws IOException {
        // create authorization request
        AuthorizationRequest request = new AuthorizationRequest();
        // set ticket and claims format
        request.setTicket(ticket);
        // this is actually a simple claims format, not claims token format - see
        // keycloak docs
        request.setClaimTokenFormat("urn:ietf:params:oauth:token-type:jwt");

        // create and set pushed claims
        List<String> pushedClaimsList = Arrays.asList(claimsTokenResponse.claims_token);
        Map<String, List<String>> pushedClaimsMap = new HashMap<>();
        pushedClaimsMap.put("claims_token", pushedClaimsList);
        String pushedClaims = Base64.encodeBytes(JsonSerialization.writeValueAsBytes(pushedClaimsMap));
        // pushed claims
        request.setClaimToken(pushedClaims); // don't confuse pushed claims with the claims for the ticket
                                             // `request.setClaims(claims)` - it is a Keycloak feature;

        // get rpt
        AuthorizationResponse authorizationResponse = rpAuthzClient.authorization().authorize(request);
        return authorizationResponse.getToken();
    }
}
