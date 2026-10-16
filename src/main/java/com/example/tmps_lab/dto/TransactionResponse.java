package com.example.tmps_lab.dto;

import java.util.UUID;

public record TransactionResponse(
    UUID id,
    UUID accountId,
    long amount
) {};
