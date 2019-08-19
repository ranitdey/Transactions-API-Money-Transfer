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

@Path("transaction")
@Produces(MediaType.APPLICATION_JSON)
public class TransactionController {

    private TransactionService transactionsService = TransactionService.getInstance(new CurrencyConverterService());

    @POST()
    public Response createTransaction(Transaction transaction)  {
        transaction = transactionsService.createTransaction(transaction);
        return Response.ok().entity(transaction).build();
    }

    @GET()
    public Response getAllTransaction()
    {
        return Response.ok().entity(transactionsService.getAllTransactions()).build();
    }
}


