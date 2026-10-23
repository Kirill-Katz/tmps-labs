package com.example.tmps_lab.service;

import com.example.tmps_lab.dto.TransactionCommand;
import com.example.tmps_lab.model.Transaction;
import com.example.tmps_lab.model.TransactionType;
import com.example.tmps_lab.service.processor.TransactionProcessor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class TransactionDispatcher {
    private final Map<TransactionType, TransactionProcessor> processors;

    public TransactionDispatcher(List<TransactionProcessor> implementations) {
        this.processors = implementations.stream()
            .collect(Collectors.toMap(
                TransactionProcessor::type,
                Function.identity()
            ));
    }

    @Transactional
    public Transaction process(TransactionCommand command) {
        TransactionProcessor processor = processors.get(command.type());

        if (processor == null) {
            throw new IllegalArgumentException("Unsupported transaction type");
        }

        return processor.execute(command);
    }
}
