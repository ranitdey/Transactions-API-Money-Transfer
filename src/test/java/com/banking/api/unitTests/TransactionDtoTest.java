package com.banking.api.unitTests;

import com.banking.api.dto.AccountDto;
import com.banking.api.dto.TransactionDto;
import com.banking.api.models.Account;
import com.banking.api.models.Currency;
import com.banking.api.models.Transaction;
import com.banking.api.models.TransactionStatus;
import com.banking.api.services.AccountService;
import com.banking.api.services.CurrencyConverterService;
import org.hamcrest.Matchers;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class TransactionDtoTest {

    private TransactionDto transactionDto;
    private Collection<Transaction> testList;
    private CurrencyConverterService moneyExchangeService = new CurrencyConverterService();

    private static final Integer TRANSACTION_1_ID = 1;
    private static final Integer TRANSACTION_2_ID = 2;
    AccountService bankAccountService;

    private Transaction transaction1;
    private Transaction transaction2;
    private Account ranitAccount;
    private Account saketAccount;

    @BeforeClass
    public void initTestData() {
        transactionDto = new TransactionDto(moneyExchangeService);
        bankAccountService = AccountService.getInstance();

        transaction1 = new Transaction(
                "123",
                "321",
                BigDecimal.ONE,
                Currency.USD);
        transaction1.setId(TRANSACTION_1_ID);

        transaction2 = new Transaction(
                "321",
                "123",
                BigDecimal.TEN,
                Currency.USD);
        transaction2.setId(TRANSACTION_2_ID);

        testList = Arrays.asList(transaction1, transaction2);
        transactionDto.addTransaction(transaction1);
        transactionDto.addTransaction(transaction2);
        ranitAccount = new Account(
                "Ranit",
                BigDecimal.valueOf(2000L),
                BigDecimal.ZERO,
                Currency.USD
        );
        ranitAccount.setId("123");

        saketAccount = new Account(
                "saket",
                BigDecimal.valueOf(2000L),
                BigDecimal.ZERO,
                Currency.USD
        );
        saketAccount.setId("321");
        bankAccountService.createNewAccount(ranitAccount);
        bankAccountService.createNewAccount(saketAccount);
    }

    @Test(description = "Tests that all transactions from Concurrent hash map will be returned")
    public void testGetAllTransactions() {
        Collection<Transaction> resultList = transactionDto.getAllTransactionsFromDb();
        assertNotNull(resultList);
        assertEquals(testList, resultList);
    }

    @Test(description = "Tests that all transaction's id with particular status will be returned")
    public void testGetAllTransactionIdsByStatus() {
        Collection<Transaction> resultTransactionIds = transactionDto.getAllTransactionsByStatus(TransactionStatus.STARTED);

        assertNotNull(resultTransactionIds);
        assertEquals(resultTransactionIds.size(), 2);
        Boolean firstTransaction = resultTransactionIds.stream()
                .anyMatch(transaction -> transaction.getId().equals(TRANSACTION_1_ID));
        Boolean secondTransaction = resultTransactionIds.stream()
                .anyMatch(transaction -> transaction.getId().equals(TRANSACTION_2_ID));
        assertTrue(firstTransaction);
        assertTrue(secondTransaction);
    }

    @Test(description = "Tests for creating transactions")
    public void transactionCreationTest(){
        TransactionDto transactionDto = TransactionDto.getInstance(moneyExchangeService);
        AccountDto bankAccountDto = AccountDto.getInstance();

        Account ranit = bankAccountDto.getAccountFromDb(ranitAccount.getId());
        Account saket = bankAccountDto.getAccountFromDb(saketAccount.getId());

        transactionDto.addTransaction(transaction1);

        Transaction updatedTransaction = transactionDto.getTransaction(transaction1.getId());

        assertEquals(updatedTransaction.getStatus(), TransactionStatus.STARTED);
        assertEquals(updatedTransaction.getFromAccountId(),ranit.getId());
        assertEquals(updatedTransaction.getToAccountId(),saket.getId());

    }

    @Test(description = "Test to check if transactions are getting executed by the executor service")
    public void testTransactionExecution() {
        TransactionDto transactionDto = TransactionDto.getInstance(moneyExchangeService);
        AccountDto bankAccountDto = AccountDto.getInstance();

        Account ranit = bankAccountDto.getAccountFromDb(ranitAccount.getId());
        Account saket = bankAccountDto.getAccountFromDb(saketAccount.getId());

        BigDecimal ranitInitialBalance = ranit.getBalance();
        BigDecimal ranitInitialBlocked = ranit.getBlockedAmount();
        BigDecimal saketInitialBalance = saket.getBalance();
        BigDecimal saketInitialBlocked = saket.getBlockedAmount();

        transactionDto.addTransaction(transaction2);
        transactionDto.performTransaction(transaction2);

        Transaction resultTransaction = transactionDto.getTransaction(transaction2.getId());
        ranit = bankAccountDto.getAccountFromDb(transaction2.getFromAccountId());
        saket = bankAccountDto.getAccountFromDb(transaction2.getToAccountId());
        BigDecimal needToWithdraw = moneyExchangeService.exchange(
                transaction2.getAmount(),
                transaction2.getCurrency(),
                ranit.getCurrency()
        );
        BigDecimal needToTransfer = moneyExchangeService.exchange(
                transaction2.getAmount(),
                transaction2.getCurrency(),
                saket.getCurrency()
        );

        assertEquals(resultTransaction.getStatus(), TransactionStatus.SUCCEED);
        assertThat(ranitInitialBalance.subtract(needToWithdraw), Matchers.comparesEqualTo(ranit.getBalance()));
        assertThat(ranitInitialBlocked, Matchers.comparesEqualTo(ranit.getBlockedAmount()));
        assertThat(saketInitialBalance.add(needToTransfer), Matchers.comparesEqualTo(saket.getBalance()));
        assertThat(saketInitialBlocked, Matchers.comparesEqualTo(saket.getBlockedAmount()));
    }

}
