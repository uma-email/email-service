package org.acme.emailservice.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
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
import org.acme.emailservice.rest.client.ClaimsProviderRestClient;
import org.acme.emailservice.security.Claims;
import org.acme.emailservice.security.ClaimsTokenResponse;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.keycloak.authorization.client.AuthzClient;
import org.keycloak.common.util.Base64;
import org.keycloak.common.util.Base64Url;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.authorization.AuthorizationRequest;
import org.keycloak.representations.idm.authorization.AuthorizationResponse;
import org.keycloak.representations.idm.authorization.PermissionRequest;
import org.keycloak.representations.idm.authorization.PermissionResponse;

import org.keycloak.util.JsonSerialization;

@ApplicationScoped
public class MessageService {

    private static Logger log = Logger.getLogger(Message.class);
    
    @PersistenceContext
    EntityManager em;

    AuthzClient rsAuthzClient = AuthzClient.create(Thread.currentThread().getContextClassLoader().getResourceAsStream("keycloak-rs-service.json"));

    AuthzClient rpAuthzClient = AuthzClient.create(Thread.currentThread().getContextClassLoader().getResourceAsStream("keycloak-rp-agent.json"));

    public static final String SCOPE_MESSAGE_CREATE = "message:create";
    public static final String SCOPE_MESSAGE_VIEW = "message:view";
    public static final String SCOPE_MESSAGE_DELETE = "message:delete";


    @Inject
    @RestClient
    ClaimsProviderRestClient claimsProviderRestClient;

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
          getRequestingPartyToken(username, updatedMessage);
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

    private void getRequestingPartyToken(String username, Message message) {
        try {
            //----------------------------------rs----------------------------------------------

            // generate ticket verifier
            String randomUUID = UUID.randomUUID().toString();
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            String input = randomUUID;
            byte[] check = md.digest(input.getBytes(StandardCharsets.UTF_8));
            String ticketVerifier = Base64Url.encode(check); // aka uma ticket - see the keycloak uma protocol implementation differencies          

            // create ticket challenge 
            String ticketChallenge = Base64Url.encode(md.digest(ticketVerifier.getBytes(StandardCharsets.UTF_8)));

            // get resource id of incomming box
            String resourceId = rsAuthzClient.protection().resource().findByName("Incoming Box").getId();

            //----------------------------------rp----------------------------------------------
           
            // get rp access token
            AccessTokenResponse accessToken = rpAuthzClient.obtainAccessToken();
            log.info("RP Access Token: "  + accessToken.getToken());

            // create claims
            Claims claims = new Claims(ticketChallenge, username);
            // get signed claims token from a claims provider
            ClaimsTokenResponse claimsTokenResponse = claimsProviderRestClient.getClaimsToken("Bearer "  + accessToken.getToken(), claims);
            log.info("Claims Token: "  + claimsTokenResponse.claims_token);

            //----------------------------------rs----------------------------------------------

            // create permission request
            PermissionRequest permissionRequest = new PermissionRequest(resourceId);
            permissionRequest.addScope(SCOPE_MESSAGE_CREATE);
            permissionRequest.setClaim("ticket_verifier", ticketVerifier);

            // get ticket with ticket verifier - see the keycloak uma protocol implementation differencies
            PermissionResponse pmResponse = rsAuthzClient.protection().permission().create(permissionRequest);
            log.info("Ticket: "  + pmResponse.getTicket());
            
            //----------------------------------rp----------------------------------------------
           
            // create authorization request
            AuthorizationRequest request = new AuthorizationRequest();
            // set ticket and claims format
            request.setTicket(pmResponse.getTicket());
            // this is actually a simple claims format, not claims token format - see keycloak docs
            request.setClaimTokenFormat("urn:ietf:params:oauth:token-type:jwt");

            // create and set pushed claims
            List<String> pushedClaimsList = Arrays.asList(claimsTokenResponse.claims_token);
            Map<String, List<String>> pushedClaimsMap = new HashMap<>();            
            pushedClaimsMap.put("claims_token", pushedClaimsList);
            String pushedClaims = Base64.encodeBytes(JsonSerialization.writeValueAsBytes(pushedClaimsMap));
            // pushed claims
            request.setClaimToken(pushedClaims); // don't confuse pushed claims with the claims for the ticket `request.setClaims(claims)` - it is a Keycloak feature;
    
            // get rpt
            AuthorizationResponse authorizationResponse = rpAuthzClient.authorization().authorize(request);
            String token = authorizationResponse.getToken();
            log.info("RPT Token: "  + token);

        } catch (Exception e) {
            throw new RuntimeException("Could not create RPT.", e);
        }
    }
}
