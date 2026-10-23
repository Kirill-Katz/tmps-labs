package com.example.tmps_lab.model;

import java.util.UUID;

public record DepositTransaction(UUID id, UUID toAccountId, long amount) implements Transaction {
    @Override
    public UUID fromAccountId() {
        return null;
    }

    @Override
    public TransactionType type() {
        return TransactionType.DEPOSIT;
    }
}
