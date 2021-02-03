package org.acme.emailservice.service;

import java.util.List;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import org.acme.emailservice.exception.EmailServiceException;
import org.acme.emailservice.model.Account;
import org.acme.emailservice.model.Attachment;
import org.acme.emailservice.model.Label;
import org.acme.emailservice.model.Message;
import org.acme.emailservice.model.RecipientBcc;
import org.acme.emailservice.model.RecipientCc;
import org.acme.emailservice.model.RecipientTo;
import org.acme.emailservice.model.Tag;
// import org.jboss.logging.Logger;
import org.acme.emailservice.model.User;

@ApplicationScoped
public class MessageService {

    // private static Logger log = Logger.getLogger(Message.class);

    @PersistenceContext
    EntityManager em;

    public Message getMessage(String username, Long id) {
        Message result = em.createNamedQuery("Message.get", Message.class).setParameter("username", username)
                .setParameter("id", id).getSingleResult();
        return result;
    }

    public List<Message> getMessages(String username) {
        return (List<Message>) em.createNamedQuery("Message.getAll", Message.class).setParameter("username", username)
                .getResultList();
    }

    public List<Message> getMessages(String username, Account account) {
        Account accountByEmailAddress = em.createNamedQuery("Account.getByEmailAddress", Account.class)
                .setParameter("username", username).setParameter("emailAddress", account.getEmailAddress())
                .getSingleResult();

        return (List<Message>) em.createNamedQuery("Message.getAllByAccount", Message.class)
                .setParameter("username", username).setParameter("account", accountByEmailAddress).getResultList();
    }

    // TODO: User/Role for message, labels, ...
    @Transactional
    public Message createMessage(String username, Account account, Message message) throws EmailServiceException {
        User user = em.createNamedQuery("User.getUserByUsername", User.class).setParameter("username", username)
                .getSingleResult();
        // Account
        Account accountByEmailAddress = em.createNamedQuery("Account.getByEmailAddress", Account.class)
                .setParameter("username", username).setParameter("emailAddress", account.getEmailAddress())
                .getSingleResult();

        Message newMessage = new Message();

        newMessage.setAccount(accountByEmailAddress);
        // Subject
        newMessage.setSubject(message.getSubject());
        // RecipientsTo
        for (RecipientTo recipientTo : message.getRecipientsTo()) {
            recipientTo.setMessage(newMessage);
        }
        newMessage.getRecipientsTo().addAll(message.getRecipientsTo());
        // RecipientsCc
        for (RecipientCc recipientCc : message.getRecipientsCc()) {
            recipientCc.setMessage(newMessage);
        }
        newMessage.getRecipientsCc().addAll(message.getRecipientsCc());
        // RecipientsBcc
        for (RecipientBcc recipientBcc : message.getRecipientsBcc()) {
            recipientBcc.setMessage(newMessage);
        }
        newMessage.getRecipientsBcc().addAll(message.getRecipientsBcc());
        // Attachments
        // TODO separated method
        for (Attachment attachment : message.getAttachments()) {
            attachment.setMessage(newMessage);
        }
        newMessage.getAttachments().addAll(message.getAttachments());
        // Tags
        for (Tag tag : message.getTags()) {
            tag.setMessage(newMessage);
        }
        newMessage.getTags().addAll(message.getTags());
        // Labels
        if (message.getLabels().size() > 0) {
            throw new EmailServiceException("labels for draft are not allowed");
        }
        Label draftLabel = em.createNamedQuery("Label.getUserDraftsLabel", Label.class).setParameter("user", user)
                .getSingleResult();
        newMessage.addLabel(draftLabel);
        // HistoryId
        newMessage.setHistoryId(Long
                .parseLong(em.createNativeQuery("select nextval('MESSAGE_HISTORY_ID')").getSingleResult().toString()));
        // TimelineId
        newMessage.setTimelineId(Long
                .parseLong(em.createNativeQuery("select nextval('MESSAGE_TIMELINE_ID')").getSingleResult().toString()));
        newMessage.setLastStmt((byte) 0);

        em.persist(newMessage);
        return newMessage;
    }

    // TODO: User/Role for message, labels, ...
    @Transactional
    public Message updateMessage(String username, Message message) throws EmailServiceException {
        if (message.getId() == null) {
            throw new EmailServiceException("id is required");
        }
        boolean updateHistory = false;
        boolean updateTimeline = false;
        Message oldMessage = em.createNamedQuery("Message.get", Message.class).setParameter("username", username)
                .setParameter("id", message.getId()).getSingleResult();
        if (oldMessage.getSentAt() == null) {
            // Subject
            if (message.getSubject() != null && (!oldMessage.getSubject().equals(message.getSubject()))) {
                updateHistory = true;
                updateTimeline = true;
                oldMessage.setSubject(message.getSubject());
            }
            // RecipientsTo
            for (RecipientTo recipientTo : message.getRecipientsTo()) {
                recipientTo.setMessage(oldMessage);
            }
            List<RecipientTo> recipientsTo = message.getRecipientsTo();
            List<RecipientTo> oldRecipientsTo = oldMessage.getRecipientsTo();
            boolean recipientToEquals = recipientsTo.containsAll(oldRecipientsTo) && oldRecipientsTo.containsAll(recipientsTo);
            if (recipientsTo != null && !recipientToEquals) {
                updateHistory = true;
                updateTimeline = true;
                oldMessage.getRecipientsTo().clear();
                oldMessage.getRecipientsTo().addAll(message.getRecipientsTo());
            }
            // RecipientsCc
            for (RecipientCc recipientCc : message.getRecipientsCc()) {
                recipientCc.setMessage(oldMessage);
            }
            List<RecipientCc> recipientsCc = message.getRecipientsCc();
            List<RecipientCc> oldRecipientsCc = oldMessage.getRecipientsCc();
            boolean recipientCcEquals = recipientsCc.containsAll(oldRecipientsCc) && oldRecipientsCc.containsAll(recipientsCc);
            if (recipientsCc != null && !recipientCcEquals) {
                updateHistory = true;
                updateTimeline = true;
                oldMessage.getRecipientsCc().clear();
                oldMessage.getRecipientsCc().addAll(message.getRecipientsCc());
            }
            // RecipientsBcc
            for (RecipientBcc recipientBcc : message.getRecipientsBcc()) {
                recipientBcc.setMessage(oldMessage);
            }
            List<RecipientBcc> recipientsBcc = message.getRecipientsBcc();
            List<RecipientBcc> oldRecipientsBcc = oldMessage.getRecipientsBcc();
            boolean recipientBccEquals = recipientsBcc.containsAll(oldRecipientsBcc) && oldRecipientsBcc.containsAll(recipientsBcc);
            if (recipientsBcc != null && !recipientBccEquals) {
                updateHistory = true;
                updateTimeline = true;
                oldMessage.getRecipientsBcc().clear();
                oldMessage.getRecipientsBcc().addAll(message.getRecipientsBcc());
            }
            // Attachments
            // TODO separated method
            for (Attachment attachment : message.getAttachments()) {
                attachment.setMessage(oldMessage);
            }
            List<Attachment> attachments = message.getAttachments();
            List<Attachment> oldAttachments = oldMessage.getAttachments();
            boolean attachmentEquals = attachments.containsAll(oldAttachments) && oldAttachments.containsAll(attachments);
            if (attachments != null && !attachmentEquals) {
                updateHistory = true;
                updateTimeline = true;
                oldMessage.getAttachments().clear();
                oldMessage.getAttachments().addAll(message.getAttachments());
            }
            // Tags
            for (Tag tag : message.getTags()) {
                tag.setMessage(oldMessage);
            }
            List<Tag> tags = message.getTags();
            List<Tag> oldTags = oldMessage.getTags();
            boolean tagEquals = tags.containsAll(oldTags) && oldTags.containsAll(tags);
            if (tags != null && !tagEquals) {
                updateHistory = true;
                updateTimeline = true;
                oldMessage.getTags().clear();
                oldMessage.getTags().addAll(message.getTags());
            }
            // Labels
            if (message.getLabels().size() > 0) {
                throw new EmailServiceException("labels for draft are not allowed");
            }
        } else {
            // lazy load
            // RecipientsTo
            @SuppressWarnings("unused")
            List<RecipientTo> oldRecipientsTo = oldMessage.getRecipientsTo();
            // RecipientsCc
            @SuppressWarnings("unused")
            List<RecipientCc> oldRecipientsCc = oldMessage.getRecipientsCc();
            // RecipientsBcc
            @SuppressWarnings("unused")
            List<RecipientBcc> oldRecipientsBcc = oldMessage.getRecipientsBcc();
            // Attachments
            @SuppressWarnings("unused")
            List<Attachment> oldAttachments = oldMessage.getAttachments();
            // Tags
            @SuppressWarnings("unused")
            List<Tag> oldTags = oldMessage.getTags();
        }
        // Labels
        // TODO: insert new labels into label table
        Set<Label> labels = message.getLabels();
        Set<Label> oldLabels = oldMessage.getLabels();
        boolean labelEquals = labels.containsAll(oldLabels) && oldLabels.containsAll(labels);
        if (labels != null && !labelEquals) {
            updateHistory = true;
            oldMessage.getLabels().clear();
            for (Label l : message.getLabels()) {
                Label label = em.find(Label.class, l.getId());
                if (label != null) {
                    oldMessage.addLabel(label);
                }
            }
        }
        // HistoryId
        if (updateHistory) {
            Long value = Long.parseLong(
                    em.createNativeQuery("select nextval('MESSAGE_HISTORY_ID')").getSingleResult().toString());
            oldMessage.setHistoryId(value);
            oldMessage.setLastStmt((byte) 1);
        }
        // TimelineId
        if (updateTimeline) {
            Long value = Long.parseLong(
                    em.createNativeQuery("select nextval('MESSAGE_TIMELINE_ID')").getSingleResult().toString());
            oldMessage.setTimelineId(value);
        }
        return em.merge(oldMessage);
    }

    @Transactional
    public Message delete(String username, Long id) {
        Message result = em.createNamedQuery("Message.get", Message.class).setParameter("username", username)
                .setParameter("id", id).getSingleResult();

        if (result != null) {
            em.remove(result);
        }
        return result;
    }
}
