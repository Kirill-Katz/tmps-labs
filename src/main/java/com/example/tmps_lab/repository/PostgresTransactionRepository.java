package com.example.tmps_lab.repository;

import com.example.tmps_lab.model.DepositTransaction;
import com.example.tmps_lab.model.Transaction;
import com.example.tmps_lab.model.TransactionType;
import com.example.tmps_lab.model.TransferTransaction;
import com.example.tmps_lab.model.WithdrawalTransaction;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class PostgresTransactionRepository implements TransactionRepository {
    private final JdbcTemplate jdbc;

    public PostgresTransactionRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Transaction save(Transaction transaction) {
        jdbc.update("""
            INSERT INTO transactions (id, from_account_id, to_account_id, type, amount)
            VALUES (?, ?, ?, ?, ?)
            ON CONFLICT (id) DO UPDATE SET
                from_account_id = EXCLUDED.from_account_id,
                to_account_id = EXCLUDED.to_account_id,
                type = EXCLUDED.type,
                amount = EXCLUDED.amount
            """, transaction.id(), transaction.fromAccountId(), transaction.toAccountId(),
            transaction.type().name(), transaction.amount());
        return transaction;
    }

    @Override
    public Transaction findById(UUID id) {
        List<Transaction> results = jdbc.query("""
            SELECT id, from_account_id, to_account_id, type, amount
            FROM transactions WHERE id = ?
            """, (rs, row) -> {
                UUID transactionId = rs.getObject("id", UUID.class);
                UUID fromId = rs.getObject("from_account_id", UUID.class);
                UUID toId = rs.getObject("to_account_id", UUID.class);
                long amount = rs.getLong("amount");
                return switch (TransactionType.valueOf(rs.getString("type"))) {
                    case DEPOSIT -> new DepositTransaction(transactionId, toId, amount);
                    case WITHDRAWAL -> new WithdrawalTransaction(transactionId, fromId, amount);
                    case TRANSFER -> new TransferTransaction(transactionId, fromId, toId, amount);
                };
            }, id);
        return results.isEmpty() ? null : results.getFirst();
    }
}
