package org.acme.emailservice.service;

import javax.enterprise.context.ApplicationScoped;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import org.acme.emailservice.model.Account;
import org.acme.emailservice.model.AccountInit;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

@ApplicationScoped
public class AccountInitService {

    private static Logger log = Logger.getLogger(AccountInitService.class);

    @ConfigProperty(name = "ACCOUNTS_INIT")
    String accountsInitConfig;

    @PersistenceContext
    EntityManager em;

    @Transactional
    public void persistAccount() {
        Jsonb jsonb = JsonbBuilder.create();
        AccountInit[] accountInitArray = jsonb.fromJson(accountsInitConfig, new AccountInit[] {}.getClass());
        log.debug(jsonb.toJson(accountInitArray));
        for (AccountInit accountInit : accountInitArray) {
            Account accountByEmailAddress = em.createNamedQuery("Account.getByEmailAddress", Account.class)
            .setParameter("username", accountInit.getUsername()).setParameter("emailAddress", accountInit
            .getEmail_address())
            .getSingleResult();
            if (accountByEmailAddress != null) {
                log.debug(jsonb.toJson(accountInit));
                accountByEmailAddress.setOAuth2Provider(accountInit.getProvider());
                accountByEmailAddress.setOAuth2Scope(accountInit.getScopes().toString());
                accountByEmailAddress.setOAuth2AccessToken(accountInit.getAccess_token());
                accountByEmailAddress.setOAuth2RefreshToken(accountInit.getRefresh_token());
                accountByEmailAddress.setOAuth2ExpiryDate(accountInit.getExpiry_date());
                em.persist(accountByEmailAddress);
            }
        }
    }
}
