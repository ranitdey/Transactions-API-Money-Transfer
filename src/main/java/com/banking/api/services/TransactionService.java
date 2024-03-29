package com.banking.api.services;

import com.banking.api.dto.TransactionDto;
import com.banking.api.models.Transaction;
import com.banking.api.models.TransactionStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

public class TransactionService {

    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);

    private static AtomicInteger atomicInteger = new AtomicInteger(0);
    private static ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    private TransactionDto transactionDto;
    private static TransactionService transactionService;


    /**
     * @return It returns the object of this TransactionService class.
     */
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

    public TransactionService(TransactionDto transactionDto) {
        this.transactionDto = transactionDto;
        executorService.scheduleAtFixedRate(() ->
                        transactionService.executeTransactions(),
                1, 3, TimeUnit.SECONDS);
        log.info("Starting transaction executor service with initial delay of 1 second and " +
                "time interval of 3 seconds ");
    }

    /**
     * this method runs asynchronously in scheduled time period.The job of this method is to execute
     * all the transactions
     */
    public void executeTransactions() {
        synchronized (this)
        {
            log.info("Transaction executor invoked. Starting executing transactions");
            List<Transaction> allExecutableTransaction = transactionDto.getAllTransactionsByStatus(TransactionStatus.STARTED);
            allExecutableTransaction
                    .forEach(transaction -> transactionDto.performTransaction(transaction));
            log.info("Transaction execution cycle completed");
        }
    }

    /**
     * This method validates if a transaction is a valid transaction and adds the transaction into
     * the list of transaction with the status STARTED.This transaction will be picked up by an
     * executor later.
     * @param transaction Transction which will be pushed for execution.
     * @return This returns the updated transaction.
     */
    public Transaction createTransaction(Transaction transaction) {
        log.info("Initialising transaction creation process for transaction");
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
            log.info("Added transaction successfully to executable transaction list");
        }
        return transaction;
    }

    /**
     *  This method returns the list of all the transactions from the history.
     * @return
     */
    public List<Transaction> getAllTransactions()
    {
        return transactionDto.getAllTransactionsFromDb();
    }

    /**
     * This method returns specific transaction by searching for it with id.
     * @param id ID of the transaction to be searched for.
     * @return This returns the Transaction object by searching it with its id.
     */
    public Transaction getTransactionById(Integer id)
    {
        if (id!=null)
        {
            return transactionDto.getTransaction(id);
        }
        else
            {
            throw new WebApplicationException("Transaction do not exist",Response.Status.BAD_REQUEST);
            }

    }
}
