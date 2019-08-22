package com.banking.api.integrationTests;

import com.banking.api.models.Account;
import com.banking.api.models.Currency;
import com.banking.api.models.Transaction;
import com.banking.api.services.AccountService;
import com.banking.api.services.CurrencyConverterService;
import com.banking.api.services.TransactionService;
import org.hamcrest.Matchers;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicInteger;
import static org.hamcrest.MatcherAssert.assertThat;

public class ConcurrentTransactionsTests {

    private TransactionService transactionsService = TransactionService.getInstance(new CurrencyConverterService());
    private AccountService bankAccountService = AccountService.getInstance();

    private static final BigDecimal INITIAL_BALANCE = BigDecimal.valueOf(10000L);
    private static final BigDecimal TRANSACTION_AMOUNT = BigDecimal.ONE;
    private static final int INVOCATION_COUNT = 10000;

    private String fromBankAccountId;
    private String toBankAccountId;
    private AtomicInteger invocationsDone = new AtomicInteger(0);

    @BeforeClass
    public void initializeData()  {
        Account fromBankAccount = new Account(
                "Bank Account 1",
                INITIAL_BALANCE,
                BigDecimal.ZERO,
                Currency.EUR
        );

        Account toBankAccount = new Account(
                "New Bank Account 2",
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                Currency.USD
        );

        fromBankAccountId = fromBankAccount.getId();
        bankAccountService.createNewAccount(fromBankAccount);
        toBankAccountId = toBankAccount.getId();
        bankAccountService.createNewAccount(toBankAccount);
    }

    @Test(threadPoolSize = 200, invocationCount = INVOCATION_COUNT)
    public void ConcurrentTransactionsTest() {

        int currentTestNumber = invocationsDone.addAndGet(1);
        Transaction transaction = new Transaction(
                fromBankAccountId,
                toBankAccountId,
                TRANSACTION_AMOUNT,
                Currency.EUR
        );
        transactionsService.createTransaction(transaction);
        if (currentTestNumber % 5 == 0) {
            transactionsService.executeTransactions();
        }
    }

    @AfterClass
    public void validateResults() {
        transactionsService.executeTransactions();
        Account fromBankAccount = bankAccountService.findAccount(fromBankAccountId);
        assertThat(fromBankAccount.getBalance(),
                Matchers.comparesEqualTo(
                        INITIAL_BALANCE.subtract(
                                TRANSACTION_AMOUNT.multiply(BigDecimal.valueOf(INVOCATION_COUNT)))
                )
        );
        assertThat(fromBankAccount.getBlockedAmount(), Matchers.comparesEqualTo(BigDecimal.ZERO));
    }
}
