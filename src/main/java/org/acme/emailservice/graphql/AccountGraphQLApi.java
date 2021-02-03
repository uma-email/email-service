package org.acme.emailservice.graphql;

import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;

import org.acme.emailservice.model.Account;
import org.acme.emailservice.service.AccountService;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;
import org.eclipse.microprofile.jwt.Claim;
import org.eclipse.microprofile.jwt.Claims;

@GraphQLApi
@RolesAllowed({ "user", "admin" })
public class AccountGraphQLApi {

    @Inject
    @Claim(standard = Claims.preferred_username)
    String username;

    @Inject
    AccountService accountService;

    @Query
    public String helloAccount() {
        return "Hello Account!";
    }

    @Query
    public Account getAccount(Long id){
        return accountService.getAccount(username, id);
    }

    @Query
    public List<Account> getAccounts() {
        return accountService.getAccounts(username);
    }
}