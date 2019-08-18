package com.banking.api.dto;

import com.banking.api.models.Account;
import java.util.concurrent.ConcurrentHashMap;

public class AccountDto {


    private ConcurrentHashMap<String, Account> accountDetails = new ConcurrentHashMap<>();

    private static final AccountDto accountDto= new AccountDto();

    public static AccountDto getInstance() {
        return accountDto;
    }

    public Account getAccountFromDb(String id)
    {
        return accountDetails.get(id);
    }

    public Account createEntryInDb(Account account)
    {
        return accountDetails.put(account.getId(),account);
    }

    public Account removeEntryFromDb(String id)
    {
        return accountDetails.remove(id);
    }

    public Account updateAccountFromDb(Account account)
    {
        return accountDetails.put(account.getId(),account);
    }

    boolean checkIfAccountExists(String id)
    {
        return accountDetails.containsKey(id);
    }
}
