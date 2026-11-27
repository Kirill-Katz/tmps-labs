package com.example.tmps_lab;

import com.example.tmps_lab.model.Account;
import com.example.tmps_lab.repository.AccountRepository;
import com.example.tmps_lab.repository.PostgresAccountRepository;
import java.sql.ResultSet;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AccountRepositoryAdapterTests {
    private final JdbcTemplate jdbc = mock(JdbcTemplate.class);
    private final AccountRepository adapter = new PostgresAccountRepository(jdbc);

    @Test
    void translatesDomainSaveIntoParameterizedUpsert() {
        Account account = new Account(UUID.randomUUID(), 75);

        assertSame(account, adapter.save(account));

        var sql = ArgumentCaptor.forClass(String.class);
        verify(jdbc).update(sql.capture(), eq(account.id()), eq(account.balance()));
        assertTrue(sql.getValue().contains("INSERT INTO accounts (id, balance) VALUES (?, ?)"));
        assertTrue(sql.getValue().contains("ON CONFLICT (id) DO UPDATE"));
    }

    @Test
    void mapsJdbcRowIntoAccountAndRetainsRowLock() throws Exception {
        UUID id = UUID.randomUUID();
        ResultSet row = mock(ResultSet.class);
        when(row.getObject("id", UUID.class)).thenReturn(id);
        when(row.getLong("balance")).thenReturn(75L);
        when(jdbc.query(anyString(), org.mockito.ArgumentMatchers.<RowMapper<Account>>any(), eq(id)))
            .thenAnswer(call -> {
                RowMapper<Account> mapper = call.getArgument(1);
                return List.of(mapper.mapRow(row, 0));
            });

        assertEquals(new Account(id, 75), adapter.findById(id));

        var sql = ArgumentCaptor.forClass(String.class);
        verify(jdbc).query(sql.capture(), org.mockito.ArgumentMatchers.<RowMapper<Account>>any(), eq(id));
        assertTrue(sql.getValue().contains("WHERE id = ? FOR UPDATE"));
    }

    @Test
    void convertsEmptyJdbcResultIntoMissingAccount() {
        UUID id = UUID.randomUUID();
        when(jdbc.query(anyString(), org.mockito.ArgumentMatchers.<RowMapper<Account>>any(), eq(id)))
            .thenReturn(List.of());
        assertNull(adapter.findById(id));
    }

    @Test
    void preservesDatabaseFailures() {
        Account account = new Account(UUID.randomUUID(), 75);
        var failure = new DataAccessResourceFailureException("database unavailable");
        when(jdbc.update(anyString(), eq(account.id()), eq(account.balance()))).thenThrow(failure);
        assertSame(failure, assertThrows(DataAccessResourceFailureException.class, () -> adapter.save(account)));
    }
}
