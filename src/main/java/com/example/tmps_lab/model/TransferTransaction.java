package com.example.tmps_lab.model;

import java.util.UUID;

public record TransferTransaction(UUID id, UUID fromAccountId, UUID toAccountId, long amount) implements Transaction {
    @Override
    public TransactionType type() {
        return TransactionType.TRANSFER;
    }
}
