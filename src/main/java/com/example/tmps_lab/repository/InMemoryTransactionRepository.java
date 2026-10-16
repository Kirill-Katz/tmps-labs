package com.example.tmps_lab.repository;

import com.example.tmps_lab.model.Transaction;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Repository
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
