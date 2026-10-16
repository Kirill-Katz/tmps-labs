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
public class TransferProcessor implements TransactionProcessor {
    private final TransactionRepository transRepo;
    private final AccountRepository accRepo;

    public TransferProcessor(
        TransactionRepository transRepo,
        AccountRepository accRepo
    ) {
        this.transRepo = transRepo;
        this.accRepo = accRepo;
    }

    @Override
    public TransactionType type() {
        return TransactionType.TRANSFER;
    }

    @Override
    public Transaction process(TransactionCommand command) {
        if (command.amount() <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        UUID fromAccountId = command.fromAccountId();
        UUID toAccountId = command.toAccountId();

        if (fromAccountId == null || toAccountId == null) {
            throw new IllegalArgumentException("Both from and to account id are required");
        }

        if (fromAccountId.equals(toAccountId)) {
            throw new IllegalArgumentException("Cannot transfer to the same account");
        }

        Account fromAccount = accRepo.findById(fromAccountId);
        if (fromAccount == null) {
            throw new IllegalArgumentException("From account not found");
        }

        Account toAccount = accRepo.findById(toAccountId);
        if (toAccount == null) {
            throw new IllegalArgumentException("To account not found");
        }

        if (fromAccount.balance() < command.amount()) {
            throw new IllegalArgumentException("Insufficient funds for transfer");
        }

        long newToBalance = Math.addExact(toAccount.balance(), command.amount());
        long newFromBalance = Math.subtractExact(fromAccount.balance(), command.amount());

        accRepo.save(new Account(fromAccountId, newFromBalance));
        accRepo.save(new Account(toAccountId, newToBalance));

        Transaction transaction = new Transaction(
            UUID.randomUUID(),
            fromAccountId,
            toAccountId,
            TransactionType.TRANSFER,
            command.amount()
        );

        return transRepo.save(transaction);
    }
}
