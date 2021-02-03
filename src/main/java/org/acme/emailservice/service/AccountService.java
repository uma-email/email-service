package org.acme.emailservice.service;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.acme.emailservice.model.Account;

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
}
