package com.example.tmps_lab.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.example.tmps_lab.dto.TransactionCommand;
import com.example.tmps_lab.dto.RepeatTransactionRequest;
import com.example.tmps_lab.dto.TransactionResponse;
import com.example.tmps_lab.service.TransactionService;

import java.util.UUID;

@RestController
@RequestMapping("/transactions")
public class TransactionController {
    private final TransactionService service;

    public TransactionController(TransactionService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    public TransactionResponse getTransaction(@PathVariable UUID id) {
        return TransactionResponse.from(service.get(id));
    }

    @PostMapping
    public TransactionResponse createTransaction(
        @RequestBody TransactionCommand command
    ) {
        return TransactionResponse.from(service.create(command));
    }

    @PostMapping("/{id}/repeat")
    public TransactionResponse repeatTransaction(
        @PathVariable UUID id,
        @RequestBody RepeatTransactionRequest request
    ) {
        return TransactionResponse.from(service.repeat(id, request.amount()));
    }
}
