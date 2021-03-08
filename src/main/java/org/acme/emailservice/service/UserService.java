package org.acme.emailservice.service;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import org.acme.emailservice.model.User;

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

    @Transactional
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
}
