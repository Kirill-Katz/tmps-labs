package com.example.tmps_lab.dto;

import java.util.UUID;

public record AccountResponse(
    UUID id,
    long amount
) {};
