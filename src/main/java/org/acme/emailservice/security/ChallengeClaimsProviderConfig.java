package org.acme.emailservice.security;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jwk.RsaJwkGenerator;

@Singleton
public class ChallengeClaimsProviderConfig {

    RsaJsonWebKey key;

    @Inject
    @ConfigProperty(name = "mp.jwt.verify.publickey.location")
    String issuer;

    @PostConstruct
    public void init() throws Exception {
        key = RsaJwkGenerator.generateJwk(2048);
        key.setUse("sig");
        key.setKeyId("1");
        key.setAlgorithm("RS256");
    }

    public String getIssuer() {
        return issuer;
    }

    public RsaJsonWebKey getKey() {
        return key;
    }
}
