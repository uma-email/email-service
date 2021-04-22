package org.acme.emailservice.service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

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
import org.jboss.logging.Logger;
import org.acme.emailservice.model.User;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.authorization.client.AuthzClient;
import org.keycloak.authorization.client.ClientAuthorizationContext;
import org.keycloak.authorization.client.resource.AuthorizationResource;
import org.keycloak.common.util.Base64Url;
import org.keycloak.common.util.RandomString;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.idm.authorization.AuthorizationRequest;
import org.keycloak.representations.idm.authorization.AuthorizationResponse;
import org.keycloak.representations.idm.authorization.JSPolicyRepresentation;
import org.keycloak.representations.idm.authorization.Permission;
import org.keycloak.representations.idm.authorization.PermissionRequest;
import org.keycloak.representations.idm.authorization.PermissionResponse;
import org.keycloak.representations.idm.authorization.PermissionTicketRepresentation;
import org.keycloak.representations.idm.authorization.PermissionTicketToken;
import org.keycloak.representations.idm.authorization.ResourceRepresentation;
import org.keycloak.representations.idm.authorization.ScopeRepresentation;
import org.keycloak.util.JsonSerialization;

@ApplicationScoped
public class MessageService {

    private static Logger log = Logger.getLogger(Message.class);
    
    @PersistenceContext
    EntityManager em;

    AuthzClient rsAuthzClient = AuthzClient.create(Thread.currentThread().getContextClassLoader().getResourceAsStream("keycloak-rs.json"));

    AuthzClient rpAuthzClient = AuthzClient.create(Thread.currentThread().getContextClassLoader().getResourceAsStream("keycloak-rp.json"));

    public static final String SCOPE_MESSAGE_VIEW = "message:view";
    public static final String SCOPE_MESSAGE_DELETE = "message:delete";

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
        Account accountByUserAndEmailAddress = em.createNamedQuery("Account.getByUserAndEmailAddress", Account.class)
                .setParameter("username", username).setParameter("emailAddress", account.getEmailAddress())
                .getSingleResult();

        return (List<Message>) em.createNamedQuery("Message.getAllByAccount", Message.class)
                .setParameter("username", username).setParameter("account", accountByUserAndEmailAddress).getResultList();
    }

    // TODO: User/Role for message, labels, ...
    @Transactional
    public Message createMessage(String username, Account account, Message message) throws EmailServiceException {
        User user = em.createNamedQuery("User.getUserByUsername", User.class).setParameter("username", username)
                .getSingleResult();
        // Account
        Account accountByUserAndEmailAddress = em.createNamedQuery("Account.getByUserAndEmailAddress", Account.class)
                .setParameter("username", username).setParameter("emailAddress", account.getEmailAddress())
                .getSingleResult();

        Message newMessage = new Message();

        newMessage.setAccount(accountByUserAndEmailAddress);
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
    public Message updateMessage(String username, Message message, boolean send) throws EmailServiceException {
        if (message.getId() == null) {
            throw new EmailServiceException("id is required");
        }
        boolean updateHistory = false;
        boolean updateTimeline = false;
        Message oldMessage = em.createNamedQuery("Message.get", Message.class).setParameter("username", username)
                .setParameter("id", message.getId()).getSingleResult();
        if (oldMessage.getSentAt() == null) {
            // it is a message draft
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
            if (send && recipientsTo == null) {
                throw new EmailServiceException("at least one recipientsTo is required");
            }
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
            // it is a sent message
            if (send) {
                throw new EmailServiceException("only message draft can be sent");
            }
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
        // Send
        if (send) {
            // TODO set system labels (remove DRAFTS, add SENT)
            oldMessage.setSentAt(LocalDateTime.now());
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
        Message updatedMessage = em.merge(oldMessage);

        if (send) {
          // TODO: create a message copy for each recipient and add OUTGOING label ???
          // TODO create a resource for each message copy on Resource Server ???
          createProtectedResource(username, updatedMessage);
          // TODO: send an authorization email via SMTP distributor, if needed send a fallback authorization email to it's own robot
        }

        return updatedMessage;
    }

    @Transactional
    public Message delete(String username, Long id) throws EmailServiceException {
        Message message = em.createNamedQuery("Message.get", Message.class).setParameter("username", username)
                .setParameter("id", id).getSingleResult();

        if (message != null) {
            if (message.getSentAt() == null) {
                em.remove(message);
            } else {
                // TODO remove from Trash
                throw new EmailServiceException("not implemented");
            }
        }
        return message;
    }

    private void createProtectedResource(String username, Message message) {
        try {
            HashSet<ScopeRepresentation> scopes = new HashSet<ScopeRepresentation>();

            scopes.add(new ScopeRepresentation(SCOPE_MESSAGE_VIEW));
            scopes.add(new ScopeRepresentation(SCOPE_MESSAGE_DELETE));

            Map<String, List<String>> attributes = new HashMap<>();
            attributes.put("client_id", Arrays.asList("email-rp"));
            String tac = UUID.randomUUID().toString();
            log.info("tac: "  + tac);
            attributes.put("tac", Arrays.asList(tac));

            String resourceName = UUID.randomUUID().toString();
            ResourceRepresentation messageResource = new ResourceRepresentation("message-" + resourceName, scopes, "/message/*", "http://email.com/message");

            messageResource.setOwner(username);
            messageResource.setAttributes(attributes);
            messageResource.setOwnerManagedAccess(true);

            ResourceRepresentation rsResponse = rsAuthzClient.protection().resource().create(messageResource);

            message.setResourceId(rsResponse.getId());

            // -----------------------------------

            // PermissionTicketRepresentation ticket = new PermissionTicketRepresentation();
            // ticket.setResource(rsResponse.getId());
            // ticket.setScope(SCOPE_MESSAGE_VIEW);
            // ticket.setScopeName(SCOPE_MESSAGE_VIEW);
            // ticket.setOwner(username);
            // // ticket.setRequester("service-account-email-rp");
            // ticket.setRequesterName("service-account-email-rp");
            // ticket.setGranted(true);

            /* AuthorizationRequest request = new AuthorizationRequest();

            AuthorizationResponse response2 = rsAuthzClient.authorization("igor.zboran@gmail.com", "password").authorize(request);

            rsAuthzClient.protection(response2.getToken()).permission().create(ticket); */

            // rsAuthzClient.protection().permission().create(ticket);

            // -----------------------------------

            PermissionRequest permissionRequest = new PermissionRequest(rsResponse.getId());

            permissionRequest.addScope(SCOPE_MESSAGE_VIEW);
            permissionRequest.setClaim("tac", tac);
    
            PermissionResponse pmResponse = rsAuthzClient.protection().permission().create(permissionRequest);
            AuthorizationRequest request = new AuthorizationRequest();

            log.info("ticket: "  + pmResponse.getTicket());
    
            request.setTicket(pmResponse.getTicket());
            // http://openid.net/specs/openid-connect-core-1_0.html#IDToken
            // urn:ietf:params:oauth:token-type:jwt
            request.setClaimTokenFormat("urn:ietf:params:oauth:token-type:jwt");
            // String claimToken = rsAuthzClient.obtainAccessToken().getToken();
            String claimToken = "ewogICAib3JnYW5pemF0aW9uIjogWyJhY21lIl0KfQ==";
            // HashMap<Object, Object> obj = new HashMap<>();
            // obj.put("claim-a", "claim-a");
            // request.setClaimToken(Base64Url.encode(JsonSerialization.writeValueAsBytes(obj)));
            request.setClaimToken(claimToken);
            // Map<String, List<String>> claims = new HashMap<>();
            // claims.put("tac", Arrays.asList(tac));
            // request.setClaims(claims);
            // request.setClaimToken(rpAuthzClient.obtainAccessToken().getToken());
    
            AuthorizationResponse authorizationResponse = rpAuthzClient.authorization().authorize(request);

            String token = authorizationResponse.getToken();

            log.info("token: "  + token);

            // AccessToken token = toAccessToken(authorizationResponse.getToken());
            // Collection<Permission> permissions = token.getAuthorization().getPermissions();

        } catch (Exception e) {
            throw new RuntimeException("Could not register protected resource.", e);
        }
    }
}
