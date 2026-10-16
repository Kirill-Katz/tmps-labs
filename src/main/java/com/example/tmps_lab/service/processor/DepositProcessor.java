package com.example.tmps_lab.service.processor;

import org.springframework.stereotype.Component;

import com.example.tmps_lab.dto.TransactionCommand;
import com.example.tmps_lab.model.TransactionType;
import com.example.tmps_lab.model.Transaction;
import com.example.tmps_lab.model.Account;
import com.example.tmps_lab.repository.AccountRepository;
import com.example.tmps_lab.repository.TransactionRepository;

import java.util.UUID;

@Component
public class DepositProcessor implements TransactionProcessor {
    private final TransactionRepository transRepo;
    private final AccountRepository accRepo;

    public DepositProcessor(
        TransactionRepository transRepo,
        AccountRepository accRepo
    ) {
        this.transRepo = transRepo;
        this.accRepo = accRepo;
    }

    @Override
    public TransactionType type() {
        return TransactionType.DEPOSIT;
    }

    @Override
    public Transaction process(TransactionCommand command) {
        if (command.amount() <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        UUID accountId = command.toAccountId();
        if (accountId == null) {
            throw new IllegalArgumentException("Destination account is required");
        }

        Account account = accRepo.findById(accountId);
        if (account == null) {
            throw new IllegalArgumentException("Account not found");
        }

        long newBalance = Math.addExact(account.balance(), command.amount());
        accRepo.save(new Account(accountId, newBalance));

        Transaction transaction = new Transaction(
            UUID.randomUUID(),
            null,
            accountId,
            TransactionType.DEPOSIT,
            command.amount()
        );

        return transRepo.save(transaction);
    }
}
