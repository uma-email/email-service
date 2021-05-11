package org.acme.emailservice.service;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class WellKnownService {

    public String extractDomainNameFromEmail(String emailAddress) {
        return emailAddress.substring(emailAddress.indexOf("@") + 1);
    }
}
