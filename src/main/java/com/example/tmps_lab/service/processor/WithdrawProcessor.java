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
public class WithdrawProcessor implements TransactionProcessor {
    private final TransactionRepository transRepo;
    private final AccountRepository accRepo;

    public WithdrawProcessor(
        TransactionRepository transRepo,
        AccountRepository accRepo
    ) {
        this.transRepo = transRepo;
        this.accRepo = accRepo;
    }

    @Override
    public TransactionType type() {
        return TransactionType.WITHDRAWAL;
    }

    @Override
    public Transaction process(TransactionCommand command) {
        if (command.amount() <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        UUID accountId = command.fromAccountId();

        if (accountId == null) {
            throw new IllegalArgumentException("Source account is required");
        }

        Account account = accRepo.findById(accountId);
        if (account == null) {
            throw new IllegalArgumentException("Account not found");
        }

        if (account.balance() < command.amount()) {
            throw new IllegalArgumentException("Insufficient funds");
        }

        long newBalance = Math.subtractExact(
            account.balance(),
            command.amount()
        );

        accRepo.save(new Account(accountId, newBalance));

        Transaction transaction = new Transaction(
            UUID.randomUUID(),
            accountId,
            null,
            TransactionType.WITHDRAWAL,
            command.amount()
        );

        return transRepo.save(transaction);
    }
}
