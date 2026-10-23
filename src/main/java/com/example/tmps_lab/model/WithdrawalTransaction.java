package com.example.tmps_lab.model;

import java.util.UUID;

public record WithdrawalTransaction(UUID id, UUID fromAccountId, long amount) implements Transaction {
    @Override
    public UUID toAccountId() {
        return null;
    }

    @Override
    public TransactionType type() {
        return TransactionType.WITHDRAWAL;
    }
}
