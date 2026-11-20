package com.example.tmps_lab.event;

import com.example.tmps_lab.model.Transaction;
import java.util.Objects;

/** Published after processing and saving; observers wait for the surrounding commit. */
public record TransactionCompletedEvent(Transaction transaction) {
    public TransactionCompletedEvent {
        Objects.requireNonNull(transaction, "transaction");
    }
}
