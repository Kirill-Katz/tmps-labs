package com.example.tmps_lab.service;

import com.example.tmps_lab.repository.AccountRepository;
import com.example.tmps_lab.model.Account;

import org.springframework.stereotype.Service;
import java.util.UUID;


@Service
public class AccountService {
    private final AccountRepository repository;

    public AccountService(AccountRepository repository) {
        this.repository = repository;
    }

    public Account create() {
        Account account = new Account(UUID.randomUUID(), 0);
        return repository.save(account);
    }

    public Account get(UUID id) {
        return repository.findById(id);
    }
}
