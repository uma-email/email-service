package org.acme.emailservice.security;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.smallrye.jwt.build.Jwt;

@Singleton
public class ChallengeClaimsProviderService {
    
    @Inject
    ChallengeClaimsProviderConfig claimsConfig;

    public String generateToken(ChallengeClaims claims) {
        return Jwt.issuer(claimsConfig.getIssuer())
                .upn(claims.emailAddress)
                .groups("user")
                .claim("ticket_digest", claims.ticketDigest)
                .claim("email_address", claims.emailAddress)
                .claim("ecosystem_type", claims.ecosystemType)
                .jws().keyId("1")
                .sign(claimsConfig.getKey().getPrivateKey());
    }
}
