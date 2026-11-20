package com.example.tmps_lab.event;

import com.example.tmps_lab.model.TransactionType;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.atomic.LongAdder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/** Process-local counters: reset on restart and are not shared across instances. */
@Component
public class TransactionMetricsListener {
    private final Map<TransactionType, LongAdder> completed = new EnumMap<>(TransactionType.class);

    public TransactionMetricsListener() {
        for (TransactionType type : TransactionType.values()) {
            completed.put(type, new LongAdder());
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTransactionCompleted(TransactionCompletedEvent event) {
        completed.get(event.transaction().type()).increment();
    }

    public long completedCount(TransactionType type) {
        return completed.get(type).sum();
    }
}
