package com.example.tmps_lab.service.processor;

import com.example.tmps_lab.dto.TransactionCommand;
import com.example.tmps_lab.model.TransactionType;
import com.example.tmps_lab.model.Transaction;
import com.example.tmps_lab.repository.TransactionRepository;
import java.util.UUID;

public abstract class TransactionProcessor {
    private final TransactionRepository transRepo;

    protected TransactionProcessor(TransactionRepository transRepo) {
        this.transRepo = transRepo;
    }

    public abstract TransactionType type();

    public final Transaction execute(TransactionCommand command) {
        process(command);
        Transaction transaction = createTransaction(UUID.randomUUID(), command);
        return transRepo.save(transaction);
    }

    protected abstract void process(TransactionCommand command);

    protected abstract Transaction createTransaction(UUID id, TransactionCommand command);
}
