package com.example.tmps_lab.model;

import java.util.UUID;

public sealed interface Transaction permits DepositTransaction, WithdrawalTransaction, TransferTransaction {
    UUID id();

    UUID fromAccountId();

    UUID toAccountId();

    TransactionType type();

    long amount();
}
