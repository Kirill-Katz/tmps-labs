package com.example.tmps_lab.model;

import com.example.tmps_lab.dto.TransactionCommand;
import java.util.UUID;

// Prototype for recurring transactions: copies contain instructions, never a transaction ID.
public record TransactionTemplate(
    TransactionType type,
    UUID fromAccountId,
    UUID toAccountId,
    long amount
) {
    public static TransactionTemplate from(Transaction transaction) {
        return new TransactionTemplate(
            transaction.type(),
            transaction.fromAccountId(),
            transaction.toAccountId(),
            transaction.amount()
        );
    }

    public TransactionTemplate copy() {
        return new TransactionTemplate(type, fromAccountId, toAccountId, amount);
    }

    public TransactionTemplate copyWithAmount(long newAmount) {
        return new TransactionTemplate(type, fromAccountId, toAccountId, newAmount);
    }

    public TransactionCommand toCommand() {
        return TransactionCommand.builder()
            .type(type)
            .fromAccountId(fromAccountId)
            .toAccountId(toAccountId)
            .amount(amount)
            .build();
    }
}
