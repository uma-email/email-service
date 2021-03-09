package org.acme.emailservice.service;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

import org.acme.emailservice.model.Account;
import org.acme.emailservice.model.Label;
import org.acme.emailservice.model.User;
import org.acme.emailservice.model.enums.ELabelRole;

@ApplicationScoped
public class UserService {

    @PersistenceContext
    EntityManager em;

    public User getUser(Long id) {
        User result = em.createNamedQuery("User.get", User.class).setParameter("id", id).getSingleResult();
        return result;
    }

    public User getUserByUsername(String username) {
        User result = em.createNamedQuery("User.getUserByUsername", User.class).setParameter("username", username).getSingleResult();
        return result;
    }

    public List<User> getUsers() {
        return (List<User>) em.createNamedQuery("User.getAll", User.class).getResultList();
    }

    public User findOrCreate(String username) {
        try {
            User user = em.createNamedQuery("User.getUserByUsername", User.class).setParameter("username", username).getSingleResult();
            return user;
        } catch (NoResultException ex) {
            User user = new User();
            user.setUsername(username);
            em.persist(user);
            return user;
        }
    }

    public User findOrCreateProfile(String username, String emailAddress) {
        User user = findOrCreate(username);

        if (user.getLabels().isEmpty()) {
            for (ELabelRole labelRole : ELabelRole.values()) {
                Label label = new Label();
                label.setUser(user);
                if (labelRole.toString().startsWith("CATEGORY_")) { 
                    String labelName = labelRole.toString().substring(("CATEGORY_".length())).toLowerCase();
                    label.setName(labelName.substring(0, 1).toUpperCase() + labelName.substring(1));
                } else {
                    String labelName = labelRole.toString().toLowerCase();
                    label.setName(labelName.substring(0, 1).toUpperCase() + labelName.substring(1));
                }
                label.setRole(labelRole);
                // HistoryId
                label.setHistoryId(Long
                        .parseLong(em.createNativeQuery("select nextval('LABEL_HISTORY_ID')").getSingleResult().toString()));
                // TimelineId
                label.setLastStmt((byte) 0);
                em.persist(label);
                user.addLabel(label);    
            }
        }

        boolean accountFound = false;

        for (Account account : user.getAccounts()) {
            if (account.getEmailAddress().equals(emailAddress)) {
                accountFound = true;
                break;
            }
        }

        if (!accountFound) {
            Account account = new Account();
            account.setEmailAddress(emailAddress);
            account.setUsername(emailAddress);
            user.addAccount(account);
        }

        return user;
    }
}
