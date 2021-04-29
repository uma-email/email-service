package org.acme.emailservice.security;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.smallrye.jwt.build.Jwt;

@Singleton
public class ClaimsService {
    
    @Inject
    ClaimsConfig claimsConfig;

    public String generateToken(Claims claims) {
        return Jwt.issuer(claimsConfig.getIssuer())
                .upn(claims.emailAddress)
                .groups("user")
                .claim("code_challenge", claims.codeChallenge)
                .claim("email_address", claims.emailAddress)
                .jws().keyId("1")
                .sign(claimsConfig.getKey().getPrivateKey());
    }
}
