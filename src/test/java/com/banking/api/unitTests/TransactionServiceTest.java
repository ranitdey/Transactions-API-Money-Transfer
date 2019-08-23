package com.banking.api.unitTests;

import com.banking.api.dto.TransactionDto;
import com.banking.api.models.Account;
import com.banking.api.models.Currency;
import com.banking.api.models.Transaction;
import com.banking.api.models.TransactionStatus;
import com.banking.api.services.AccountService;
import com.banking.api.services.TransactionService;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

public class TransactionServiceTest {

    private String fromBankAccountId;
    private String toBankAccountId;
    private AccountService bankAccountService = AccountService.getInstance();

    @BeforeClass
    public void initializeData()  {
        Account fromBankAccount = new Account(
                "Bank Account 1",
                BigDecimal.valueOf(300L),
                BigDecimal.ZERO,
                Currency.EUR
        );

        Account toBankAccount = new Account(
                "Bank Account 2",
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                Currency.USD
        );

        fromBankAccountId = fromBankAccount.getId();
        bankAccountService.createNewAccount(fromBankAccount);
        toBankAccountId = toBankAccount.getId();
        bankAccountService.createNewAccount(toBankAccount);
    }


    /**
     * Testing of Transaction creation and execution. Once  the transaction has been created
     * the scheduled job will execute it asynchronously and will update the transaction status.
     *
     */
    @Test
    public void createAndExecuteTransactionTest() {

        Integer TRANSACTION_ID = 123;
        TransactionDto transactionDto = mock(TransactionDto.class);

        Transaction transaction = new Transaction(
                fromBankAccountId,
                toBankAccountId,
                BigDecimal.TEN,
                Currency.USD
        );
        transaction.setId(TRANSACTION_ID);

        when(transactionDto.getAllTransactionsByStatus(any())).thenReturn(
                (Collections.singletonList(transaction)
                ));
        doAnswer(invocation -> {
            transaction.setStatus(TransactionStatus.SUCCEED);
            return null;
        }).when(transactionDto).performTransaction(transaction);
        TransactionService transactionsService = new TransactionService(transactionDto);
        Transaction createdTransaction = transactionsService.createTransaction(transaction);
        assertEquals(createdTransaction, transaction);
        assertEquals(createdTransaction.getStatus(), TransactionStatus.STARTED);
        transactionsService.executeTransactions();
        assertEquals(transaction.getStatus(), TransactionStatus.SUCCEED);
    }

    @Test
    public void getAllTransactionsTest(){
        TransactionDto transactionDto = mock(TransactionDto.class);
        TransactionService transactionsService = new TransactionService(transactionDto);

        List<Transaction> testList = Arrays.asList(
                new Transaction(
                        toBankAccountId,
                        fromBankAccountId,
                        BigDecimal.ZERO,
                        Currency.USD),
                new Transaction(
                        fromBankAccountId,
                        fromBankAccountId,
                        BigDecimal.ZERO,
                        Currency.USD)
        );

        when(transactionDto.getAllTransactionsFromDb()).thenReturn(testList);
        Collection<Transaction> transactions = transactionsService.getAllTransactions();
        assertNotNull(transactions);
        assertArrayEquals(testList.toArray(), transactions.toArray());
    }
}
