package org.acme.emailservice.email;

import io.quarkus.arc.config.ConfigProperties;

@ConfigProperties(prefix = "email")
public interface Config {

    public String getUser();
    public String getPassword();

    public String getSmtpHost();
    public boolean getAuth();
    public boolean getStarttls();
    public int getPort();

    public String getFrom();
    public String getFromDisplayName();

    public String getInvitationSubject();
    public String getNotificationSubject();
}
