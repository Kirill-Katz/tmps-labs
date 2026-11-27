package com.example.tmps_lab;

import com.example.tmps_lab.model.DepositTransaction;
import com.example.tmps_lab.model.Transaction;
import com.example.tmps_lab.repository.LoggingTransactionRepository;
import com.example.tmps_lab.repository.TransactionRepository;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(OutputCaptureExtension.class)
class LoggingTransactionRepositoryTests {
    private final TransactionRepository delegate = mock(TransactionRepository.class);
    private final TransactionRepository decorated = new LoggingTransactionRepository(delegate);

    @Test
    void delegatesExactlyOnceAndReturnsDelegateResultsWithoutLoggingFinancialData(CapturedOutput output) {
        Transaction transaction = new DepositTransaction(UUID.randomUUID(), UUID.randomUUID(), 987654321);
        Transaction stored = new DepositTransaction(transaction.id(), transaction.toAccountId(), transaction.amount());
        when(delegate.save(transaction)).thenReturn(stored);
        when(delegate.findById(transaction.id())).thenReturn(stored);

        assertSame(stored, decorated.save(transaction));
        assertSame(stored, decorated.findById(transaction.id()));
        verify(delegate).save(transaction);
        verify(delegate).findById(transaction.id());
        verifyNoMoreInteractions(delegate);
        assertTrue(output.getOut().contains("operation=save outcome=returned durationNs="));
        assertTrue(output.getOut().contains("operation=findById outcome=returned durationNs="));
        assertFalse(output.getOut().contains(transaction.id().toString()));
        assertFalse(output.getOut().contains(transaction.toAccountId().toString()));
        assertFalse(output.getOut().contains("987654321"));
    }

    @Test
    void preservesMissingResult() {
        UUID id = UUID.randomUUID();
        assertNull(decorated.findById(id));
        verify(delegate).findById(id);
        verifyNoMoreInteractions(delegate);
    }

    @Test
    void propagatesOriginalSaveAndReadFailuresWithoutRetrying(CapturedOutput output) {
        Transaction transaction = new DepositTransaction(UUID.randomUUID(), UUID.randomUUID(), 10);
        var failure = new IllegalStateException("storage unavailable");
        when(delegate.save(transaction)).thenThrow(failure);
        when(delegate.findById(transaction.id())).thenThrow(failure);

        assertSame(failure, assertThrows(IllegalStateException.class, () -> decorated.save(transaction)));
        assertSame(failure, assertThrows(IllegalStateException.class, () -> decorated.findById(transaction.id())));
        verify(delegate).save(transaction);
        verify(delegate).findById(transaction.id());
        verifyNoMoreInteractions(delegate);
        assertTrue(output.getOut().contains("operation=save outcome=threw durationNs="));
        assertTrue(output.getOut().contains("operation=findById outcome=threw durationNs="));
    }
}
