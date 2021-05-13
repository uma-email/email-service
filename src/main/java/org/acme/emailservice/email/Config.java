package org.acme.emailservice.email;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Singleton;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.quarkus.arc.config.ConfigProperties;

// @ApplicationScoped
@ConfigProperties(prefix = "email")
public class Config {

    // @ConfigProperty(name = "SMTP_USER")
    // String getUser();

    // @ConfigProperty(name = "SMTP_PASSWORD")
    // String getPassword();

    // @ConfigProperty(name = "SMTP_USER")
    // public String smtpUser;

    @ConfigProperty(name = "SMTP_USER")
    public Optional<String> user;

    @ConfigProperty(name = "SMTP_PASSWORD")
    public Optional<String> password;

    public String smtpHost;
    public boolean auth;
    public boolean ssl;
    public boolean starttls;
    public int port;

    // String getSmtpHost();
    // boolean getAuth();
    // boolean getSsl();
    // boolean getStarttls();
    // int getPort();

    // String getFrom();
    // String getFromDisplayName();

    public String from;
    public String fromDisplayName;
    // public String replyTo;
    // public String replyToDisplayName;
    // public String envelopeFrom;

    // private String user;
    // private String password;

    public String getPort() {
        return Integer.toString(port);
    }

    // public void setPort(int port) {
    //     this.port = port;
    // }

    // public String getUser() {
    //     return user;
    // }

    // public void setUser(String user) {
    //     this.user = smtpUser;
    // }

    // public String getPassword() {
    //     return password;
    // }

    // public void setPassword(String password) {
    //     this.password = password;
    // }
}
