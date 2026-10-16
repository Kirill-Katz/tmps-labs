package com.example.tmps_lab.service;

import com.example.tmps_lab.dto.TransactionCommand;
import com.example.tmps_lab.model.Transaction;
import com.example.tmps_lab.repository.TransactionRepository;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class TransactionService {
    private final TransactionDispatcher dispatcher;
    private final TransactionRepository repository;

    public TransactionService(
        TransactionDispatcher dispatcher,
        TransactionRepository repository
    ) {
        this.dispatcher = dispatcher;
        this.repository = repository;
    }

    public Transaction create(TransactionCommand command) {
        return dispatcher.process(command);
    }

    public Transaction get(UUID id) {
        return repository.findById(id);
    }
}
