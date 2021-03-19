package org.acme.emailservice.email;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.acme.emailservice.model.Account;
import org.jboss.logging.Logger;

import jakarta.mail.Folder;

@ApplicationScoped
public class EmailService {

    private static Logger log = Logger.getLogger(EmailService.class);

    @PersistenceContext
    EntityManager em;

    public Folder inbox;

    Thread[] threads;

    public EmailService() {

        log.info("EmailService created");
    }

    public void start() {

        log.info("EmailService started");
        log.info(em);

        List<Account> accounts = em.createQuery("SELECT a FROM Account a", Account.class).getResultList();

        log.info("accounts.size(): " + accounts.size());

        threads = new Thread[accounts.size()];
        int i = 0;
        for (Account account : accounts) {
            switch (String.valueOf(account.getOAuth2Provider())) {
                case "gmail":
                    threads[i] = new Thread((Runnable) new GmailClient(account));
                    break;
                case "outlook":
                    threads[i] = new Thread((Runnable) new OutlookClient(account));
                    break;
                default:
                    threads[i] = new Thread((Runnable) new EmailClient(account));
            }
            threads[i].start();
            i++;
        }
    }

    public void shutdown() throws InterruptedException {
        log.info("EmailService shutdown");
        /* GmailClient.die = true;
        OutlookClient.die = true;
        EmailClient.die = true; */
        BaseClassClient.die = true;
        // wait for all the threads to finish
        // for (int i = 0; i < nthreads; i++) {
        // threads[i].interrupt();
        // }
        for (int i = 0; i < threads.length; i++) {
            threads[i].join();
        }
    }
}
