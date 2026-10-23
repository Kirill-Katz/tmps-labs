package com.example.tmps_lab.repository;

import com.example.tmps_lab.model.Transaction;

import java.util.UUID;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class InMemoryTransactionRepository implements TransactionRepository {
    private final Map<UUID, Transaction> transactions =
        new ConcurrentHashMap<>();

    @Override
    public Transaction save(Transaction transaction) {
        transactions.put(transaction.id(), transaction);
        return transaction;
    }

    @Override
    public Transaction findById(UUID id) {
        return transactions.get(id);
    }
}
