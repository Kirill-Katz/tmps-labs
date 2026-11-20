package com.example.tmps_lab;

import com.example.tmps_lab.dto.TransactionCommand;
import com.example.tmps_lab.event.TransactionAuditListener;
import com.example.tmps_lab.event.TransactionCompletedEvent;
import com.example.tmps_lab.event.TransactionMetricsListener;
import com.example.tmps_lab.model.Account;
import com.example.tmps_lab.model.Transaction;
import com.example.tmps_lab.model.TransactionType;
import com.example.tmps_lab.repository.InMemoryAccountRepository;
import com.example.tmps_lab.repository.InMemoryTransactionRepository;
import com.example.tmps_lab.service.TransactionDispatcher;
import com.example.tmps_lab.service.processor.DepositProcessor;
import com.example.tmps_lab.service.processor.TransferProcessor;
import com.example.tmps_lab.service.processor.WithdrawProcessor;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TransactionObserverTests {
    private AnnotationConfigApplicationContext context;
    private TransactionDispatcher dispatcher;
    private TransactionMetricsListener metrics;
    private TransactionAuditListener audit;
    private PublishedEvents published;
    private TransactionTemplate transaction;
    private UUID source;
    private UUID destination;

    @BeforeEach
    void setUp() {
        context = new AnnotationConfigApplicationContext(Config.class);
        dispatcher = context.getBean(TransactionDispatcher.class);
        metrics = context.getBean(TransactionMetricsListener.class);
        audit = context.getBean(TransactionAuditListener.class);
        published = context.getBean(PublishedEvents.class);
        transaction = new TransactionTemplate(context.getBean(TestTransactionManager.class));
        var accounts = context.getBean(InMemoryAccountRepository.class);
        source = UUID.randomUUID();
        destination = UUID.randomUUID();
        accounts.save(new Account(source, 100));
        accounts.save(new Account(destination, 0));
    }

    @AfterEach
    void closeContext() {
        context.close();
    }

    @ParameterizedTest
    @EnumSource(TransactionType.class)
    void notifiesBothObserversOnceAfterCommit(TransactionType type) {
        Transaction result = transaction.execute(status -> {
            Transaction saved = dispatcher.process(command(type, 10));
            assertEquals(List.of(new TransactionCompletedEvent(saved)), published.events);
            assertEquals(saved, context.getBean(InMemoryTransactionRepository.class).findById(saved.id()));
            assertEquals(0, metrics.completedCount(type));
            verify(audit, never()).onTransactionCompleted(any());
            return saved;
        });

        assertEquals(1, metrics.completedCount(type));
        verify(audit, times(1)).onTransactionCompleted(new TransactionCompletedEvent(result));
        for (TransactionType other : TransactionType.values()) {
            if (other != type) {
                assertEquals(0, metrics.completedCount(other));
            }
        }
    }

    @Test
    void dispatcherStartsItsOwnTransaction() {
        Transaction result = dispatcher.process(command(TransactionType.DEPOSIT, 10));

        assertEquals(1, metrics.completedCount(TransactionType.DEPOSIT));
        verify(audit).onTransactionCompleted(new TransactionCompletedEvent(result));
    }

    @Test
    void rollbackSuppressesBothObserversEvenWhenEventWasPublished() {
        transaction.executeWithoutResult(status -> {
            dispatcher.process(command(TransactionType.TRANSFER, 10));
            assertEquals(1, published.events.size());
            status.setRollbackOnly();
        });

        assertEquals(0, metrics.completedCount(TransactionType.TRANSFER));
        verify(audit, never()).onTransactionCompleted(any());
    }

    @Test
    void rejectedOperationDoesNotPublishCompletion() {
        assertThrows(IllegalArgumentException.class,
            () -> dispatcher.process(command(TransactionType.WITHDRAWAL, 200)));

        assertTrue(published.events.isEmpty());
        assertEquals(0, metrics.completedCount(TransactionType.WITHDRAWAL));
        verify(audit, never()).onTransactionCompleted(any());
    }

    @Test
    void failedSaveDoesNotPublishCompletion() {
        var repository = context.getBean(InMemoryTransactionRepository.class);
        doThrow(new IllegalStateException("Storage unavailable")).when(repository).save(any());

        assertThrows(IllegalStateException.class,
            () -> dispatcher.process(command(TransactionType.DEPOSIT, 10)));

        assertTrue(published.events.isEmpty());
        assertEquals(0, metrics.completedCount(TransactionType.DEPOSIT));
        verify(audit, never()).onTransactionCompleted(any());
    }

    @Test
    void eventsOutsideTransactionDoNotTriggerCommitObservers() {
        var saved = new com.example.tmps_lab.model.DepositTransaction(UUID.randomUUID(), destination, 10);
        context.publishEvent(new TransactionCompletedEvent(saved));

        assertEquals(1, published.events.size());
        assertEquals(0, metrics.completedCount(TransactionType.DEPOSIT));
        verify(audit, never()).onTransactionCompleted(any());
    }

    private TransactionCommand command(TransactionType type, long amount) {
        return switch (type) {
            case DEPOSIT -> new TransactionCommand(type, null, destination, amount);
            case WITHDRAWAL -> new TransactionCommand(type, source, null, amount);
            case TRANSFER -> new TransactionCommand(type, source, destination, amount);
        };
    }

    @Configuration
    @EnableTransactionManagement
    static class Config {
        @Bean TestTransactionManager transactionManager() { return new TestTransactionManager(); }
        @Bean InMemoryAccountRepository accounts() { return new InMemoryAccountRepository(); }
        @Bean InMemoryTransactionRepository transactions() { return spy(new InMemoryTransactionRepository()); }
        @Bean TransactionMetricsListener metrics() { return new TransactionMetricsListener(); }
        @Bean TransactionAuditListener audit() { return spy(new TransactionAuditListener()); }
        @Bean PublishedEvents published() { return new PublishedEvents(); }
        @Bean TransactionDispatcher dispatcher(
            InMemoryAccountRepository accounts, InMemoryTransactionRepository transactions,
            ApplicationEventPublisher events
        ) {
            return new TransactionDispatcher(List.of(
                new DepositProcessor(transactions, accounts),
                new WithdrawProcessor(transactions, accounts),
                new TransferProcessor(transactions, accounts)
            ), events);
        }
    }

    static class PublishedEvents {
        final List<TransactionCompletedEvent> events = new ArrayList<>();

        @EventListener
        public void onPublished(TransactionCompletedEvent event) { events.add(event); }
    }

    /** Exercises Spring synchronization only; in-memory repositories do not roll back data. */
    static class TestTransactionManager extends AbstractPlatformTransactionManager {
        private final ThreadLocal<Object> current = new ThreadLocal<>();

        @Override protected Object doGetTransaction() {
            Object existing = current.get();
            return existing != null ? existing : new Object();
        }
        @Override protected boolean isExistingTransaction(Object transaction) {
            return current.get() == transaction;
        }
        @Override protected void doBegin(Object transaction, TransactionDefinition definition) {
            current.set(transaction);
        }
        @Override protected void doCommit(DefaultTransactionStatus status) {}
        @Override protected void doRollback(DefaultTransactionStatus status) {}
        @Override protected void doCleanupAfterCompletion(Object transaction) { current.remove(); }
    }
}
