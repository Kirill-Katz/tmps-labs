package com.example.tmps_lab.repository;

import com.example.tmps_lab.model.Transaction;
import java.util.UUID;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

/** Decorates transaction storage with operation logging, not commit auditing. */
@Repository
@Primary
public class LoggingTransactionRepository implements TransactionRepository {
    private static final Logger log = LoggerFactory.getLogger(LoggingTransactionRepository.class);
    private final TransactionRepository delegate;

    public LoggingTransactionRepository(
        @Qualifier("postgresTransactionRepository") TransactionRepository delegate
    ) {
        this.delegate = delegate;
    }

    @Override
    public Transaction save(Transaction transaction) {
        return timed("save", () -> delegate.save(transaction));
    }

    @Override
    public Transaction findById(UUID id) {
        return timed("findById", () -> delegate.findById(id));
    }

    private Transaction timed(String operation, Supplier<Transaction> action) {
        long started = System.nanoTime();
        boolean returned = false;
        try {
            Transaction result = action.get();
            returned = true;
            return result;
        } finally {
            log.info("Transaction repository operation={} outcome={} durationNs={}",
                operation, returned ? "returned" : "threw", System.nanoTime() - started);
        }
    }
}
