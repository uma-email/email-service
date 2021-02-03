package org.acme.emailservice.email;

import org.acme.emailservice.model.Account;
import org.jboss.logging.Logger;

public class EmailClient extends BaseClassClient {

    private static Logger log = Logger.getLogger(EmailClient.class);

    public EmailClient(Account account) {

        super(account);
        // USER_NAME = getProperty("email.username");
        // created = true;
        // emails = new HashSet<Mail>();

        // OAuth2Authenticator.initialize();

        log.info("EmailClient created");
    }

    public void login() {
        log.info("EmailClient/SynchronizedTask login");
    }

    public void synchronize() {
        log.info("EmailClient/SynchronizedTask synchronize");
    }
}
