package com.example.tmps_lab.dto;

import com.example.tmps_lab.model.TransactionType;
import java.util.UUID;

public record TransactionCommand(
    TransactionType type,
    UUID fromAccountId,
    UUID toAccountId,
    long amount
) {
    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private TransactionType type;
        private UUID fromAccountId;
        private UUID toAccountId;
        private long amount;

        public Builder type(TransactionType type) {
            this.type = type;
            return this;
        }

        public Builder fromAccountId(UUID fromAccountId) {
            this.fromAccountId = fromAccountId;
            return this;
        }

        public Builder toAccountId(UUID toAccountId) {
            this.toAccountId = toAccountId;
            return this;
        }

        public Builder amount(long amount) {
            this.amount = amount;
            return this;
        }

        public TransactionCommand build() {
            if (type == null) {
                throw new IllegalArgumentException("Transaction type is required");
            }
            if (amount <= 0) {
                throw new IllegalArgumentException("Amount must be positive");
            }
            switch (type) {
                case DEPOSIT -> {
                    if (toAccountId == null) {
                        throw new IllegalArgumentException("Destination account is required");
                    }
                }
                case WITHDRAWAL -> {
                    if (fromAccountId == null) {
                        throw new IllegalArgumentException("Source account is required");
                    }
                }
                case TRANSFER -> {
                    if (fromAccountId == null || toAccountId == null) {
                        throw new IllegalArgumentException("Both from and to account id are required");
                    }
                    if (fromAccountId.equals(toAccountId)) {
                        throw new IllegalArgumentException("Cannot transfer to the same account");
                    }
                }
            }
            return new TransactionCommand(type, fromAccountId, toAccountId, amount);
        }
    }
}
