package com.example.tmps_lab.service.processor;

import org.springframework.stereotype.Component;

import com.example.tmps_lab.dto.TransactionCommand;
import com.example.tmps_lab.model.TransactionType;
import com.example.tmps_lab.model.Transaction;
import com.example.tmps_lab.model.TransferTransaction;
import com.example.tmps_lab.model.Account;
import com.example.tmps_lab.repository.AccountRepository;
import com.example.tmps_lab.repository.TransactionRepository;

import java.util.UUID;

@Component
public class TransferProcessor extends TransactionProcessor {
    private final AccountRepository accRepo;

    public TransferProcessor(
        TransactionRepository transRepo,
        AccountRepository accRepo
    ) {
        super(transRepo);
        this.accRepo = accRepo;
    }

    @Override
    public TransactionType type() {
        return TransactionType.TRANSFER;
    }

    @Override
    protected void process(TransactionCommand command) {
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

        // Lock both rows in a stable order to avoid deadlocks on opposite transfers.
        Account first = accRepo.findById(fromAccountId.compareTo(toAccountId) < 0 ? fromAccountId : toAccountId);
        Account second = accRepo.findById(fromAccountId.compareTo(toAccountId) < 0 ? toAccountId : fromAccountId);
        Account fromAccount = fromAccountId.compareTo(toAccountId) < 0 ? first : second;
        if (fromAccount == null) {
            throw new IllegalArgumentException("From account not found");
        }

        Account toAccount = fromAccountId.compareTo(toAccountId) < 0 ? second : first;
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

    }

    @Override
    protected Transaction createTransaction(UUID id, TransactionCommand command) {
        return new TransferTransaction(id, command.fromAccountId(), command.toAccountId(), command.amount());
    }
}
