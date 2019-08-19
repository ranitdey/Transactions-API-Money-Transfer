package com.banking.api.dto;

import com.banking.api.models.Account;
import com.banking.api.models.Transaction;
import com.banking.api.models.TransactionStatus;
import com.banking.api.services.CurrencyConverterService;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import javax.ws.rs.WebApplicationException;


public class TransactionDto {

    ConcurrentHashMap<Integer,Transaction> transactions = new ConcurrentHashMap<>();

    private CurrencyConverterService currencyConverterService;

    private TransactionDto(CurrencyConverterService currencyConverterService) {
        this.currencyConverterService = currencyConverterService;
    }

    AccountDto accountDto = AccountDto.getInstance();


    private static TransactionDto transactionDto;

    public static TransactionDto getInstance(CurrencyConverterService currencyConverterService) {
        if(transactionDto == null){
            synchronized (TransactionDto.class) {
                if(transactionDto == null){
                    transactionDto = new TransactionDto(currencyConverterService);
                }
            }
        }
        return transactionDto;
    }




    public Transaction addTransaction(Transaction transaction)
    {
        return transactions.put(transaction.getId(),transaction);
    }

    public List<Transaction> getAllTransactionsFromDb()
    {
        List<Transaction> allTransactions = new ArrayList<>();
        transactions
                .forEach((k,v)-> allTransactions.add(v));
        return allTransactions;
    }

    public List<Transaction> getAllTransactionsByStatus(TransactionStatus transactionStatus)
    {
        List<Transaction> allTransactionsByStatus = new ArrayList<>();
        transactions
                .forEach((k,v)-> {
                    if(v.getStatus()==transactionStatus)
                        allTransactionsByStatus.add(v);
                });

        return allTransactionsByStatus;
    }


    public void performTransaction(Transaction transaction) {

        if (transaction.getId() == null) {
            throw new WebApplicationException(
                    "The specified transaction doesn't exists");
        }

        try {
            transaction = getTransaction(transaction.getId());

            if (transaction.getStatus() != TransactionStatus.STARTED) {
                throw new WebApplicationException(
                        "Could not execute transaction which is not in PLANNED status");
            }

            Account fromBankAccount = accountDto.getAccountFromDb(transaction.getFromAccountId());


            Account toBankAccount = accountDto.getAccountFromDb(transaction.getToAccountId());

            BigDecimal amountToWithdraw = currencyConverterService.exchange(
                    transaction.getAmount(),
                    transaction.getCurrency(),
                    fromBankAccount.getCurrency()
            );
            BigDecimal newBlockedAmount = fromBankAccount.getBlockedAmount().add(amountToWithdraw);
            BigDecimal newBalance = fromBankAccount.getBalance().subtract(amountToWithdraw);

            if (newBlockedAmount.compareTo(fromBankAccount.getBalance()) == 1 || newBalance.compareTo(BigDecimal.ZERO) < 0) {
                transaction.setStatus(TransactionStatus.FAILED);
                transaction.setFailMessage(String.format("There is no enough money. Current balance is %f",
                        fromBankAccount.getBalance().doubleValue()));
            } else {
                fromBankAccount.setBlockedAmount(newBlockedAmount);
                fromBankAccount.setBalance(newBalance);

                accountDto.updateAccountFromDb(fromBankAccount);

                BigDecimal amountToTransfer = currencyConverterService.exchange(
                        transaction.getAmount(),
                        transaction.getCurrency(),
                        toBankAccount.getCurrency()
                );

                toBankAccount.setBalance(toBankAccount.getBalance().add(amountToTransfer));

                accountDto.updateAccountFromDb(toBankAccount);

                transaction.setStatus(TransactionStatus.SUCCEED);
            }

            updateTransaction(transaction);

        } catch (WebApplicationException e) {
            if (transaction != null) {
                transaction.setStatus(TransactionStatus.FAILED);
                transaction.setFailMessage(e.getMessage());
                updateTransaction(transaction);
            }
        }


    }

    public Transaction getTransaction(Integer id) {
        if (id==null)
        {
            throw new WebApplicationException("Id cannot be null");
        }

        return transactions.get(id);
    }

    public Transaction updateTransaction(Transaction transaction)
    {
        return transactions.put(transaction.getId(),transaction);
    }


}
