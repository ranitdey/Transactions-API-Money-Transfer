package com.banking.api.dto;

import com.banking.api.models.Account;
import java.util.concurrent.ConcurrentHashMap;

public class AccountDto {


    /**
     * ConcurrentHashMap is used for storing all the account details. It ensures thread safety.
     */
    private ConcurrentHashMap<String, Account> accountDetails = new ConcurrentHashMap<>();

    private static final AccountDto accountDto= new AccountDto();

    /**
     * @return It returns the object of this AccountDto class.
     */
    public static AccountDto getInstance() {
        return accountDto;
    }

    /**
     * @param id It takes the ID of the bank account.
     * @return This returns the account object associated with the given ID
     */
    public Account getAccountFromDb(String id)
    {
        return accountDetails.get(id);
    }

    /**
     *
     * @param account This takes an account object which needs to be saved.
     */
    public void createEntryInDb(Account account)
    {
        accountDetails.put(account.getId(),account);
    }

    /**
     *
     * @param id Account id which needs to be deleted.
     * @return This returns the account object which got deleted
     */
    public Account removeEntryFromDb(String id)
    {
        return accountDetails.remove(id);
    }

    /**
     *
     * @param account Account object which will be getting updated.
     * @return This returns the updated account object.
     */
    public Account updateAccountFromDb(Account account)
    {
        return accountDetails.put(account.getId(),account);
    }

    /**
     *
     * @param id Unique id of an account whose account existance will be checked.
     * @return Returns true if the given account exists anf false if account do not exists.
     */
    boolean checkIfAccountExists(String id)
    {
        return accountDetails.containsKey(id);
    }
}
