package com.example.tmps_lab.service.processor;

import com.example.tmps_lab.dto.TransactionCommand;
import com.example.tmps_lab.model.TransactionType;
import com.example.tmps_lab.model.Transaction;

public interface TransactionProcessor {
    TransactionType type();
    Transaction process(TransactionCommand command);
}
