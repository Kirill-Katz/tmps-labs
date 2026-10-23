package com.example.tmps_lab;

import com.example.tmps_lab.dto.TransactionCommand;
import com.example.tmps_lab.dto.TransactionResponse;
import com.example.tmps_lab.model.Account;
import com.example.tmps_lab.model.Transaction;
import com.example.tmps_lab.model.TransactionTemplate;
import com.example.tmps_lab.model.TransactionType;
import com.example.tmps_lab.model.DepositTransaction;
import com.example.tmps_lab.model.TransferTransaction;
import com.example.tmps_lab.model.WithdrawalTransaction;
import com.example.tmps_lab.repository.InMemoryAccountRepository;
import com.example.tmps_lab.repository.InMemoryTransactionRepository;
import com.example.tmps_lab.service.TransactionDispatcher;
import com.example.tmps_lab.service.TransactionService;
import com.example.tmps_lab.service.processor.DepositProcessor;
import com.example.tmps_lab.service.processor.TransferProcessor;
import com.example.tmps_lab.service.processor.WithdrawProcessor;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TransactionProcessorTests {
    @Test
    void withdrawalUsesSourceAccount() {
        var accounts = new InMemoryAccountRepository();
        var transactions = new InMemoryTransactionRepository();
        UUID accountId = UUID.randomUUID();
        accounts.save(new Account(accountId, 100));

        Transaction result = new WithdrawProcessor(transactions, accounts).execute(
            new TransactionCommand(TransactionType.WITHDRAWAL, accountId, null, 30)
        );

        assertEquals(70, accounts.findById(accountId).balance());
        assertEquals(accountId, result.fromAccountId());
        assertNull(result.toAccountId());
        TransactionResponse response = TransactionResponse.from(result);
        assertEquals(TransactionType.WITHDRAWAL, response.type());
        assertEquals(accountId, response.fromAccountId());
        assertNull(response.toAccountId());
    }

    @Test
    void depositWithoutDestinationHasUsefulError() {
        var processor = new DepositProcessor(
            new InMemoryTransactionRepository(), new InMemoryAccountRepository()
        );

        var error = assertThrows(IllegalArgumentException.class, () -> processor.execute(
            new TransactionCommand(TransactionType.DEPOSIT, null, null, 30)
        ));

        assertEquals("Destination account is required", error.getMessage());
    }

    @Test
    void builderCreatesValidCommandAndRejectsIncompleteTransfer() {
        UUID source = UUID.randomUUID();
        UUID destination = UUID.randomUUID();
        TransactionCommand command = TransactionCommand.builder()
            .type(TransactionType.TRANSFER)
            .fromAccountId(source)
            .toAccountId(destination)
            .amount(25)
            .build();

        assertEquals(source, command.fromAccountId());
        assertEquals(destination, command.toAccountId());
        assertEquals(25, command.amount());
        assertThrows(IllegalArgumentException.class, () -> TransactionCommand.builder()
            .type(TransactionType.TRANSFER).fromAccountId(source).amount(25).build());
    }

    @Test
    void processorsCreateDistinctTransactionProducts() {
        var accounts = new InMemoryAccountRepository();
        var transactions = new InMemoryTransactionRepository();
        UUID source = UUID.randomUUID();
        UUID destination = UUID.randomUUID();
        accounts.save(new Account(source, 100));
        accounts.save(new Account(destination, 0));

        assertInstanceOf(DepositTransaction.class, new DepositProcessor(transactions, accounts).execute(
            new TransactionCommand(TransactionType.DEPOSIT, null, destination, 10)));
        assertInstanceOf(WithdrawalTransaction.class, new WithdrawProcessor(transactions, accounts).execute(
            new TransactionCommand(TransactionType.WITHDRAWAL, source, null, 10)));
        assertInstanceOf(TransferTransaction.class, new TransferProcessor(transactions, accounts).execute(
            new TransactionCommand(TransactionType.TRANSFER, source, destination, 10)));
    }

    @Test
    void repeatingTransactionCopiesTemplateAndCreatesNewIdentity() {
        var accounts = new InMemoryAccountRepository();
        var transactions = new InMemoryTransactionRepository();
        UUID accountId = UUID.randomUUID();
        accounts.save(new Account(accountId, 0));
        var service = new TransactionService(new TransactionDispatcher(List.of(
            new DepositProcessor(transactions, accounts)
        )), transactions);

        Transaction original = service.create(TransactionCommand.builder()
            .type(TransactionType.DEPOSIT).toAccountId(accountId).amount(10).build());
        TransactionTemplate template = TransactionTemplate.from(original);
        TransactionTemplate changed = template.copyWithAmount(20);
        Transaction repeated = service.repeat(original.id(), 20);

        assertNotSame(template, template.copy());
        assertEquals(10, template.amount());
        assertEquals(20, changed.amount());
        assertNotEquals(original.id(), repeated.id());
        assertEquals(10, original.amount());
        assertEquals(20, repeated.amount());
        assertEquals(30, accounts.findById(accountId).balance());
    }
}
