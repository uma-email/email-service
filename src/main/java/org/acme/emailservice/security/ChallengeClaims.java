package org.acme.emailservice.security;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ChallengeClaims {
    
    public String ticketDigest;
    public String emailAddress;
    public String ecosystemType;

    public ChallengeClaims(String ticketDigest, String emailAddress, String ecosystemType) {
        this.ticketDigest = ticketDigest;
        this.emailAddress = emailAddress;
        this.ecosystemType = ecosystemType;
    }
}
