package com.example.tmps_lab.model;

import java.util.UUID;

public record Account(
    UUID id,
    long balance
) {};
