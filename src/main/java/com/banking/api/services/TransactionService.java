package com.banking.api.services;

import com.banking.api.dto.TransactionDto;
import com.banking.api.models.Transaction;
import com.banking.api.models.TransactionStatus;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicInteger;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

public class TransactionService {

    private static final TransactionService transactionService = new TransactionService();
    private static AtomicInteger atomicInteger = new AtomicInteger(0);

    public static TransactionService getInstance() {
        return transactionService;
    }
    private TransactionDto transactionDto = TransactionDto.getInstance();


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
        transactionDto.addTransaction(transaction);
        transaction.setStatus(TransactionStatus.STARTED);
        return transaction;
    }
}
