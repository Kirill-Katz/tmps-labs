package com.example.tmps_lab.event;

/** Explicit observer contract for reactions to a committed transaction. */
public interface TransactionObserver {
    void onTransactionCompleted(TransactionCompletedEvent event);
}
