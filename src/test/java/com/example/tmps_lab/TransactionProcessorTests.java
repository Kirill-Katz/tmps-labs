package com.example.tmps_lab;

import com.example.tmps_lab.dto.TransactionCommand;
import com.example.tmps_lab.model.Account;
import com.example.tmps_lab.model.Transaction;
import com.example.tmps_lab.model.TransactionType;
import com.example.tmps_lab.repository.InMemoryAccountRepository;
import com.example.tmps_lab.repository.InMemoryTransactionRepository;
import com.example.tmps_lab.service.processor.DepositProcessor;
import com.example.tmps_lab.service.processor.WithdrawProcessor;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TransactionProcessorTests {
    @Test
    void withdrawalUsesSourceAccount() {
        var accounts = new InMemoryAccountRepository();
        var transactions = new InMemoryTransactionRepository();
        UUID accountId = UUID.randomUUID();
        accounts.save(new Account(accountId, 100));

        Transaction result = new WithdrawProcessor(transactions, accounts).process(
            new TransactionCommand(TransactionType.WITHDRAWAL, accountId, null, 30)
        );

        assertEquals(70, accounts.findById(accountId).balance());
        assertEquals(accountId, result.fromAccountId());
        assertNull(result.toAccountId());
    }

    @Test
    void depositWithoutDestinationHasUsefulError() {
        var processor = new DepositProcessor(
            new InMemoryTransactionRepository(), new InMemoryAccountRepository()
        );

        var error = assertThrows(IllegalArgumentException.class, () -> processor.process(
            new TransactionCommand(TransactionType.DEPOSIT, null, null, 30)
        ));

        assertEquals("Destination account is required", error.getMessage());
    }
}
