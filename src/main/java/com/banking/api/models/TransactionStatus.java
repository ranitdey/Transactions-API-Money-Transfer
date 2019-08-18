package com.banking.api.models;

public enum TransactionStatus {
    STARTED(1), FAILED(3), SUCCEED(4);

    private int id;

    TransactionStatus(int id) {
        this.id = id;
    }

    public static TransactionStatus valueOf(int id) {
        for(TransactionStatus e : values()) {
            if(e.id == id) return e;
        }

        return null;
    }

    public int getId() {
        return id;
    }

}
