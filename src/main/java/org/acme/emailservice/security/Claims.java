package org.acme.emailservice.security;

public class Claims {
    
    public String codeChallenge;
    public String emailAddress;

    public Claims(String codeChallenge, String emailAddress) {
        this.codeChallenge = codeChallenge;
        this.emailAddress = emailAddress;
    }
}
