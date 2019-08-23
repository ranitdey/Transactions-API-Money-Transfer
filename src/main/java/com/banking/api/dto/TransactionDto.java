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

    /**
     * ConcurrentHashMap is used for storing all the transactions details. It ensures thread safety.
     */
    ConcurrentHashMap<Integer,Transaction> transactions = new ConcurrentHashMap<>();

    private CurrencyConverterService currencyConverterService;
    private static TransactionDto transactionDto;

    AccountDto accountDto = AccountDto.getInstance();

    public TransactionDto(CurrencyConverterService currencyConverterService) {
        this.currencyConverterService = currencyConverterService;
    }


    /**
     * @return It returns the object of this TransactionDto class.
     */
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


    /**
     *
     * @param transaction Takes an transaction object which will be added in the transactions list
     *                    this will be picked up later by the execute Transaction method for
     *                    execution.
     */
    public void addTransaction(Transaction transaction)
    {
        transactions.put(transaction.getId(),transaction);
    }

    /**
     *
     * @return This returns all transactions from the history.
     */
    public List<Transaction> getAllTransactionsFromDb()
    {
        List<Transaction> allTransactions = new ArrayList<>();
        transactions
                .forEach((k,v)-> allTransactions.add(v));
        return allTransactions;
    }

    /**
     *
     * @param transactionStatus Transaction status according to which the transactions will be
     *                          filtered
     * @return List of filtered transaction according to the given status
     */
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


    /**
     * This method performs the Transaction object provided. All the validations and operations
     * in order to make the transactions do happen here.Operations are:
     * <ul>
     *     <li>Add the transferring amount to blockedAmount from source Bank Account</li>
     *     <li>Check if the source bank account is having valid balance for the transaction</li>
     *     <li>Perform the transaction and update the respective bank accounts</li>
     *     <li>Change the transaction status according to the transaction success or failure </li>
     *     <li>Clear the blocked amount </li>
     * </ul>
     * We are moving the amount into the blocking state but not subtracting it from the balance until transaction will
     * not be executed.
     * For concurrency all the method which will be calling this method should take care.
     * @param transaction Transaction object which will get executed.
     */
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
                fromBankAccount.setBlockedAmount(fromBankAccount.getBlockedAmount().subtract(newBlockedAmount));
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

    /**
     *
     * @param id Unique id of a transaction.
     * @return Transaction object of the given corresponding id.
     */
    public Transaction getTransaction(Integer id) {
        if (id==null)
        {
            throw new WebApplicationException("Id cannot be null");
        }

        return transactions.get(id);
    }

    /**
     *
     * @param transaction Transaction object which will be updated
     */
    public void updateTransaction(Transaction transaction)
    {
        transactions.put(transaction.getId(),transaction);
    }


}
