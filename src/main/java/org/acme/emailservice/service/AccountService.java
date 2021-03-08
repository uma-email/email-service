package org.acme.emailservice.service;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import org.acme.emailservice.model.Account;
import org.acme.emailservice.model.User;

@ApplicationScoped
public class AccountService {

    @PersistenceContext
    EntityManager em;

    public Account getAccount(String username, Long id) {
        Account result = em.createNamedQuery("Account.get", Account.class).setParameter("username", username)
                .setParameter("id", id).getSingleResult();
        return result;
    }
    
    public List<Account> getAccounts(String username){
        return (List<Account>)em.createNamedQuery("Account.getAll", Account.class).setParameter("username", username).getResultList();        
    }

    // @Transactional
    public Account findOrCreate(User user, String emailAddress) {
        try {
            Account account = em.createNamedQuery("Account.getByUserAndEmailAddress", Account.class).setParameter("userId", user.getId()).setParameter("emailAddress", emailAddress).getSingleResult();
            return account;
        } catch (NoResultException ex) {
            Account account = new Account();
            account.setUser(user);
            account.setEmailAddress(emailAddress);
            account.setUsername(emailAddress);
            em.persist(account);
            return account;
        }
    }
}
