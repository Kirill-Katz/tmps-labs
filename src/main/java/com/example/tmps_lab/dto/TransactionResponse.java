package com.example.tmps_lab.dto;

import com.example.tmps_lab.model.Transaction;
import com.example.tmps_lab.model.TransactionType;
import java.util.UUID;

public record TransactionResponse(
    UUID id,
    UUID fromAccountId,
    UUID toAccountId,
    TransactionType type,
    long amount
) {
    public static TransactionResponse from(Transaction transaction) {
        if (transaction == null) {
            return null;
        }
        return new TransactionResponse(
            transaction.id(),
            transaction.fromAccountId(),
            transaction.toAccountId(),
            transaction.type(),
            transaction.amount()
        );
    }
}
