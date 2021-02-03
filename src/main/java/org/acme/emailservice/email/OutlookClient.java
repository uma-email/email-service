package org.acme.emailservice.email;

import org.acme.emailservice.model.Account;
import org.jboss.logging.Logger;

public class OutlookClient extends BaseClassClient {

    private static Logger log = Logger.getLogger(OutlookClient.class);

    public OutlookClient(Account account) {

        super(account);
        // USER_NAME = getProperty("email.username");
        // created = true;
        // emails = new HashSet<Mail>();

        // OAuth2Authenticator.initialize();

        log.info("OutlookClient created");
    }

    public void login() {
        log.info("OutlookClient/SynchronizedTask login");
    }

    public void synchronize() {
        log.info("OutlookClient/SynchronizedTask synchronize");
    }
}
