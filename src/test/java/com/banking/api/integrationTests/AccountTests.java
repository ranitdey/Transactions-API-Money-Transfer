package com.banking.api.integrationTests;

import com.banking.api.ApiApplication;
import com.banking.api.controllers.AccountController;
import com.banking.api.models.Account;
import com.banking.api.models.Currency;
import com.banking.api.services.AccountService;
import org.glassfish.grizzly.http.server.HttpServer;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import static org.testng.Assert.assertNotEquals;
import static org.testng.AssertJUnit.assertNotNull;

public class AccountTests {

    private static HttpServer server;
    private static WebTarget target;
    private List<String> idList= new ArrayList<>();

    @BeforeClass
    public static void setup() {
        server = ApiApplication.startServer();
        Client c = ClientBuilder.newClient();
        target = c.target(ApiApplication.BASE_URI);
    }

    @AfterClass
    public static void afterAll() {
        server.shutdownNow();
    }

    @Test(description = "Tests that all bank accounts will be returned from the database")
    public void createAccountTest() {
        AccountService bankAccountService = AccountService.getInstance();
        String OWNER_NAME = "Ranit";

        Account bankAccount = new Account(OWNER_NAME, BigDecimal.ZERO, BigDecimal.ZERO, Currency.USD);

        Response response = target.path(AccountController.BASE_URL)
                .request()
                .post(from(bankAccount));

        AssertJUnit.assertEquals(Response.Status.CREATED, response.getStatusInfo().toEnum());

        Account returnedAccount = response.readEntity(Account.class);
        Account createdAccount = bankAccountService.findAccount(returnedAccount.getId());

        assertNotNull(returnedAccount);
        assertNotNull(createdAccount);

        assertNotEquals(returnedAccount.getId(), bankAccount.getId());
        AssertJUnit.assertEquals(returnedAccount.getId(), createdAccount.getId());
        AssertJUnit.assertEquals(OWNER_NAME, createdAccount.getOwnerName());
        idList.add(returnedAccount.getId());
    }


    @Test(description = "Test if getting bank account by id is working")
    public void testGetBankAccountById() {

        Response response = target.path(AccountController.BASE_URL + "/" + idList.get(0))
                .request().get();

        AssertJUnit.assertEquals(Response.Status.OK, response.getStatusInfo().toEnum());

        Account returnedAccount = response.readEntity(Account.class);

        AssertJUnit.assertEquals(returnedAccount.getId(), idList.get(0));
        AssertJUnit.assertEquals(returnedAccount.getOwnerName(), "Ranit");
    }

    private static Entity from(Account bankAccount) {
        return Entity.entity(bankAccount, MediaType.valueOf(MediaType.APPLICATION_JSON));
    }

}
