package com.example.tmps_lab.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/** Operational audit logging, not a durable financial audit ledger. */
@Component
public class TransactionAuditListener implements TransactionObserver {
    private static final Logger log = LoggerFactory.getLogger(TransactionAuditListener.class);

    @Override
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTransactionCompleted(TransactionCompletedEvent event) {
        var transaction = event.transaction();
        log.info("Transaction committed: id={}, type={}, amount={}",
            transaction.id(), transaction.type(), transaction.amount());
    }
}
