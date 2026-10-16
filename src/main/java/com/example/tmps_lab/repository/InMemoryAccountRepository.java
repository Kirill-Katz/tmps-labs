package com.example.tmps_lab.repository;

import com.example.tmps_lab.model.Account;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Repository
public class InMemoryAccountRepository implements AccountRepository {
    private final Map<UUID, Account> accounts =
        new ConcurrentHashMap<>();

    @Override
    public Account save(Account account) {
        accounts.put(account.id(), account);
        return account;
    }

    @Override
    public Account findById(UUID id) {
        return accounts.get(id);
    }
}
