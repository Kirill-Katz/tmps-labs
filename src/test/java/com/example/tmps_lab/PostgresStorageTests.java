package com.example.tmps_lab;

import com.example.tmps_lab.controller.AccountController;
import com.example.tmps_lab.controller.TransactionController;
import com.example.tmps_lab.dto.TransactionCommand;
import com.example.tmps_lab.dto.RepeatTransactionRequest;
import com.example.tmps_lab.repository.LoggingTransactionRepository;
import com.example.tmps_lab.service.BankingFacade;
import com.example.tmps_lab.event.TransactionMetricsListener;
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
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class PostgresStorageTests {
    @Autowired AccountService accounts;
    @Autowired TransactionService transactions;
    @Autowired AccountRepository accountRepository;
    @Autowired TransactionRepository transactionRepository;
    @Autowired TransactionMetricsListener metrics;
    @Autowired PlatformTransactionManager transactionManager;
    @Autowired BankingFacade banking;
    @Autowired AccountController accountController;
    @Autowired TransactionController transactionController;

    @Test
    void springWiresFacadeAndPrimaryDecoratorIntoTheBankingFlow() {
        assertInstanceOf(LoggingTransactionRepository.class, transactionRepository);
        Account account = accountController.create();
        var deposit = transactionController.createTransaction(new TransactionCommand(
            TransactionType.DEPOSIT, null, account.id(), 20));
        var repeated = transactionController.repeatTransaction(deposit.id(), new RepeatTransactionRequest(5));

        assertEquals(25, accountController.get(account.id()).balance());
        assertEquals(deposit, transactionController.getTransaction(deposit.id()));
        assertEquals(5, banking.getTransaction(repeated.id()).amount());
        assertEquals(25, banking.getAccount(account.id()).balance());
    }

    @Test
    void completionObserversFollowDatabaseCommitAndRollback() {
        Account account = accounts.create();
        long before = metrics.completedCount(TransactionType.DEPOSIT);
        var boundary = new TransactionTemplate(transactionManager);

        Transaction rolledBack = boundary.execute(status -> {
            Transaction result = transactions.create(new TransactionCommand(
                TransactionType.DEPOSIT, null, account.id(), 25));
            assertEquals(before, metrics.completedCount(TransactionType.DEPOSIT));
            status.setRollbackOnly();
            return result;
        });

        assertNotNull(rolledBack);
        assertNull(transactionRepository.findById(rolledBack.id()));
        assertEquals(0, accountRepository.findById(account.id()).balance());
        assertEquals(before, metrics.completedCount(TransactionType.DEPOSIT));

        Transaction committed = transactions.create(new TransactionCommand(
            TransactionType.DEPOSIT, null, account.id(), 25));
        assertNotNull(transactionRepository.findById(committed.id()));
        assertEquals(25, accountRepository.findById(account.id()).balance());
        assertEquals(before + 1, metrics.completedCount(TransactionType.DEPOSIT));
    }

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
