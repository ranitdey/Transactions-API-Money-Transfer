package com.banking.api.services;

import com.banking.api.dto.TransactionDto;
import com.banking.api.models.Transaction;
import com.banking.api.models.TransactionStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

public class TransactionService {


    private static AtomicInteger atomicInteger = new AtomicInteger(0);
    private static ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    private TransactionDto transactionDto;
    private static TransactionService transactionService;


    public static TransactionService getInstance(CurrencyConverterService currencyConverterService) {
        if( transactionService== null){
            synchronized (TransactionService.class) {
                if(transactionService == null){
                    transactionService = new TransactionService(TransactionDto.getInstance(currencyConverterService));
                }
            }
        }
        return transactionService;
    }



    TransactionService(TransactionDto transactionDto) {
        this.transactionDto = transactionDto;
        executorService.scheduleAtFixedRate(() ->
                        transactionService.executeTransactions(),
                1, 3, TimeUnit.SECONDS);
    }

    private void executeTransactions() {
        synchronized (this)
        {
            List<Transaction> allExecutableTransaction = transactionDto.getAllTransactionsByStatus(TransactionStatus.STARTED);
            allExecutableTransaction
                    .forEach(transaction -> transactionDto.performTransaction(transaction));
        }

    }


    public Transaction createTransaction(Transaction transaction) {
        transaction.setId(atomicInteger.incrementAndGet());
        if (transaction.getFromAccountId() == null || transaction.getToAccountId() == null) {
            throw new WebApplicationException("The transaction has not provided from Bank Account " +
                    "or to Bank Account values",Response.Status.BAD_REQUEST);
        }
        if (transaction.getFromAccountId().equals(transaction.getToAccountId())) {

            throw new WebApplicationException("The sender and recipient should not be same",Response.Status.BAD_REQUEST);
        }
        if (transaction.getAmount().compareTo(BigDecimal.ZERO) <= 0) {

            throw new WebApplicationException("The amount should be more than 0",Response.Status.BAD_REQUEST);
        }
        synchronized (this)
        {
            transactionDto.addTransaction(transaction);
            transaction.setStatus(TransactionStatus.STARTED);
        }
        return transaction;
    }

    public List<Transaction> getAllTransactions()
    {
        return transactionDto.getAllTransactionsFromDb();
    }


}
