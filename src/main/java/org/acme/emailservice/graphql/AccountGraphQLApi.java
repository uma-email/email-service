package org.acme.emailservice.graphql;

import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;

import org.acme.emailservice.model.Account;
import org.acme.emailservice.service.AccountService;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;

import io.quarkus.security.identity.SecurityIdentity;

@GraphQLApi
@RolesAllowed({ "user", "admin" })
public class AccountGraphQLApi {

    @Inject
    SecurityIdentity identity;

    @Inject
    AccountService accountService;

    private String getUsername() {
        return identity.getPrincipal().getName();
    }

    @Query
    public String helloAccount() {
        return "Hello Account!";
    }

    @Query
    public Account getAccount(Long id){
        return accountService.getAccount(getUsername(), id);
    }

    @Query
    public List<Account> getAccounts() {
        return accountService.getAccounts(getUsername());
    }
}