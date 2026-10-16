package com.example.tmps_lab.dto;

import java.util.UUID;

public record CreateTransactionRequest(
    UUID id,
    UUID accountId,
    long amount
) {};
