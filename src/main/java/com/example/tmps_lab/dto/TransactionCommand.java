package com.example.tmps_lab.dto;

import com.example.tmps_lab.model.TransactionType;
import java.util.UUID;

public record TransactionCommand(
    TransactionType type,
    UUID fromAccountId,
    UUID toAccountId,
    long amount
) {}
