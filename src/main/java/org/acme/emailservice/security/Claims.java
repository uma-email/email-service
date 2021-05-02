package org.acme.emailservice.security;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Claims {
    
    public String ticketChallenge;
    public String emailAddress;

    public Claims(String ticketChallenge, String emailAddress) {
        this.ticketChallenge = ticketChallenge;
        this.emailAddress = emailAddress;
    }
}
