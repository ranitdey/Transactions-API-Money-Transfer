package com.banking.api.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;

public class Account {

    private String id;
    private String ownerName;
    private BigDecimal balance;
    private BigDecimal blockedAmount;
    private Currency currency;

    public Account() {
    }

    public Account(String ownerName, BigDecimal balance, BigDecimal blockedAmount, Currency currency) {
        this(UUID.randomUUID().toString(), ownerName, balance, blockedAmount, currency);
    }

    public Account(String id, String ownerName, BigDecimal balance, BigDecimal blockedAmount, Currency currency) {
        this.id = id;
        this.ownerName = ownerName;
        this.balance = balance;
        this.blockedAmount = blockedAmount;
        this.currency = currency;
    }


    public String getId() {
        return id;
    }


    public String getOwnerName() {
        return ownerName;
    }


    public BigDecimal getBalance() {
        return balance;
    }

    public BigDecimal getBlockedAmount() {
        return blockedAmount;
    }


    public Currency getCurrency() {
        return currency;
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Account that = (Account) o;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    @Override
    public String toString() {
        return "Account ID: " + this.id + " | Account owner name: " + this.ownerName +
                " | Account balance: " + this.currency + this.balance + "| Blocked amount: " + this.blockedAmount;

    }
}
