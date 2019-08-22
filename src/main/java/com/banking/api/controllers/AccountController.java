package com.banking.api.controllers;

import com.banking.api.models.Account;
import com.banking.api.services.AccountService;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/account")
@Produces(MediaType.APPLICATION_JSON)
public class AccountController {

    private final static AccountService accountService = AccountService.getInstance();

    /**
     *
     * @param accountRequest The bank account object which will get created.
     * @return This returns the bank account which got created with an unique id.
     */
    @POST
    @Path("/create")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes({MediaType.APPLICATION_JSON})
    public Response createAccount(Account accountRequest)
    {
        Account account;
        account = new Account(accountRequest.getOwnerName(),accountRequest.getBalance(),
                accountRequest.getBlockedAmount(),accountRequest.getCurrency());
        accountService.createNewAccount(account);
        return Response.accepted(account).build();
    }

    /**
     *
     * @param id Unique id that gets generated when the account is created.
     * @return Returns the accounts details corresponding to the given id.
     */
    @GET
    @Path("/{id}")
    public Response getAccountById(@PathParam("id") String id)
    {
        if (id == null)
        {
            throw new WebApplicationException("Account ID must not be empty",Response.Status.BAD_REQUEST);
        }
        return Response.ok(accountService.findAccount(id)).build();
    }

}
