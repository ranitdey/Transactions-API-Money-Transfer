package com.banking.api.dto;

import com.banking.api.models.Transaction;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class TransactionDto {

    ConcurrentHashMap<Integer,Transaction> transactions = new ConcurrentHashMap<>();

    private static final TransactionDto transactionDto= new TransactionDto();

    public static TransactionDto getInstance() {
        return transactionDto;
    }

    public Transaction addTransaction(Transaction transaction)
    {
        return transactions.put(transaction.getId(),transaction);
    }





}
