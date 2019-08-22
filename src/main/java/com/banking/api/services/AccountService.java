package com.banking.api.services;


import com.banking.api.dto.AccountDto;
import com.banking.api.models.Account;

public class AccountService {

    /**
     * This class is a thin wrapper around the respective DTO.
     */

    private static final AccountService accountService = new AccountService();

    public static AccountService getInstance() {
        return accountService;
    }

    public void createNewAccount(Account account)
    {
        AccountDto.getInstance().createEntryInDb(account);
    }

    public Account findAccount(String id)
    {
        return AccountDto.getInstance().getAccountFromDb(id);
    }


}
