package com.example.tmps_lab.repository;

import com.example.tmps_lab.model.Transaction;
import java.util.UUID;

public interface TransactionRepository {
    Transaction save(Transaction transaction);
    Transaction findById(UUID id);
}
