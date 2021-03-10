package org.acme.emailservice.graphql;

import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;

import org.acme.emailservice.exception.EmailServiceException;
import org.acme.emailservice.model.Account;
import org.acme.emailservice.model.Message;
import org.acme.emailservice.service.MessageService;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Query;

import io.quarkus.security.identity.SecurityIdentity;

@GraphQLApi
@RolesAllowed({ "user", "admin" })
public class MessageGraphQLApi {

    @Inject
    SecurityIdentity identity;

    @Inject
    MessageService messageService;

    private String getUsername() {
        return identity.getPrincipal().getName();
    }

    @Query
    public String helloMessage() {
        return "Hello Message!";
    }

    @Query
    public Message getMessage(Long id) {
        return messageService.getMessage(getUsername(), id);
    }

    @Query
    public List<Message> getMessages() {
        return messageService.getMessages(getUsername());
    }

    @Query
    public List<Message> getMessagesByAccount(Account account) {
        return messageService.getMessages(getUsername(), account);
    }

    @Mutation
    public Message createMessage(Account account, Message message) throws EmailServiceException {
        return messageService.createMessage(getUsername(), account, message);
    }

    @Mutation
    public Message updateMessage(Message message) throws EmailServiceException {
        return messageService.updateMessage(getUsername(), message, false);
    }

    @Mutation
    public Message sendMessage(Message message) throws EmailServiceException {
        return messageService.updateMessage(getUsername(), message, true);
    }

    @Mutation
    public Message deleteMessage(Long id) {
        return messageService.delete(getUsername(), id);
    }
}