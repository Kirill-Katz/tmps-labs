package com.example.tmps_lab.repository;

import com.example.tmps_lab.model.Account;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class PostgresAccountRepository implements AccountRepository {
    private final JdbcTemplate jdbc;

    public PostgresAccountRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Account save(Account account) {
        jdbc.update("""
            INSERT INTO accounts (id, balance) VALUES (?, ?)
            ON CONFLICT (id) DO UPDATE SET balance = EXCLUDED.balance
            """, account.id(), account.balance());
        return account;
    }

    @Override
    public Account findById(UUID id) {
        List<Account> results = jdbc.query(
            "SELECT id, balance FROM accounts WHERE id = ? FOR UPDATE",
            (rs, row) -> new Account(rs.getObject("id", UUID.class), rs.getLong("balance")),
            id
        );
        return results.isEmpty() ? null : results.getFirst();
    }
}
