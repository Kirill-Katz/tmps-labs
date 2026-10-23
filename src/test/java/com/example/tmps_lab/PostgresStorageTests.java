package com.example.tmps_lab;

import com.example.tmps_lab.dto.TransactionCommand;
import com.example.tmps_lab.model.Account;
import com.example.tmps_lab.model.Transaction;
import com.example.tmps_lab.model.TransactionType;
import com.example.tmps_lab.repository.AccountRepository;
import com.example.tmps_lab.repository.TransactionRepository;
import com.example.tmps_lab.service.AccountService;
import com.example.tmps_lab.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class PostgresStorageTests {
    @Autowired AccountService accounts;
    @Autowired TransactionService transactions;
    @Autowired AccountRepository accountRepository;
    @Autowired TransactionRepository transactionRepository;

    @Test
    void persistsBalancesAndBothTransactionEndpoints() {
        Account source = accounts.create();
        Account destination = accounts.create();

        transactions.create(new TransactionCommand(TransactionType.DEPOSIT, null, source.id(), 100));
        Transaction transfer = transactions.create(new TransactionCommand(
            TransactionType.TRANSFER, source.id(), destination.id(), 40));
        Transaction withdrawal = transactions.create(new TransactionCommand(
            TransactionType.WITHDRAWAL, destination.id(), null, 10));

        assertEquals(60, accountRepository.findById(source.id()).balance());
        assertEquals(30, accountRepository.findById(destination.id()).balance());
        Transaction storedTransfer = transactionRepository.findById(transfer.id());
        assertNotNull(storedTransfer);
        assertEquals(source.id(), storedTransfer.fromAccountId());
        assertEquals(destination.id(), storedTransfer.toAccountId());
        assertEquals(TransactionType.TRANSFER, storedTransfer.type());
        assertEquals(TransactionType.WITHDRAWAL, transactionRepository.findById(withdrawal.id()).type());
    }
}
