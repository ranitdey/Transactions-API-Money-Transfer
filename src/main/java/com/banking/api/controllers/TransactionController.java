package com.banking.api.controllers;


import com.banking.api.models.Transaction;
import com.banking.api.services.CurrencyConverterService;
import com.banking.api.services.TransactionService;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path(TransactionController.BASE_URL)
@Produces(MediaType.APPLICATION_JSON)
public class TransactionController {
    public static final String BASE_URL = "/transactions";

    private TransactionService transactionsService = TransactionService.getInstance(new CurrencyConverterService());

    /**
     *
     * @param transaction This accepts an transaction object which will be made.
     * @return Returns the transaction object with the status of the transaction.
     */
    @POST()
    public Response createTransaction(Transaction transaction)  {
        transaction = transactionsService.createTransaction(transaction);
        return Response.status(Response.Status.CREATED).entity(transaction).build();
    }

    /**
     *
     * @return Returns all the transaction history.
     */
    @GET()
    public Response getAllTransaction()
    {
        return Response.ok().entity(transactionsService.getAllTransactions()).build();
    }
}


