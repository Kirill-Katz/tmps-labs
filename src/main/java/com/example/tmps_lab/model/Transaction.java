package com.example.tmps_lab.model;

import java.util.UUID;

public record Transaction(
    UUID id,
    UUID fromAccountId,
    UUID toAccountId,
    TransactionType type,
    long amount
) {};
