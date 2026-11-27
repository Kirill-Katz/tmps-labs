package com.example.tmps_lab.service;

import com.example.tmps_lab.dto.TransactionCommand;
import com.example.tmps_lab.model.Account;
import com.example.tmps_lab.model.Transaction;
import com.example.tmps_lab.model.TransactionType;
import java.util.UUID;
import org.springframework.stereotype.Service;

/** Simple entry point to the account and transaction subsystems. */
@Service
public class BankingFacade {
    private final AccountService accounts;
    private final TransactionService transactions;

    public BankingFacade(AccountService accounts, TransactionService transactions) {
        this.accounts = accounts;
        this.transactions = transactions;
    }

    public Account createAccount() {
        return accounts.create();
    }

    public Account getAccount(UUID id) {
        return accounts.get(id);
    }

    public Transaction createTransaction(TransactionCommand command) {
        return transactions.create(command);
    }

    public Transaction getTransaction(UUID id) {
        return transactions.get(id);
    }

    public Transaction repeatTransaction(UUID id, long amount) {
        return transactions.repeat(id, amount);
    }

    public Transaction deposit(UUID accountId, long amount) {
        return createTransaction(TransactionCommand.builder()
            .type(TransactionType.DEPOSIT)
            .toAccountId(accountId)
            .amount(amount)
            .build());
    }

    public Transaction withdraw(UUID accountId, long amount) {
        return createTransaction(TransactionCommand.builder()
            .type(TransactionType.WITHDRAWAL)
            .fromAccountId(accountId)
            .amount(amount)
            .build());
    }

    public Transaction transfer(UUID fromAccountId, UUID toAccountId, long amount) {
        return createTransaction(TransactionCommand.builder()
            .type(TransactionType.TRANSFER)
            .fromAccountId(fromAccountId)
            .toAccountId(toAccountId)
            .amount(amount)
            .build());
    }
}
