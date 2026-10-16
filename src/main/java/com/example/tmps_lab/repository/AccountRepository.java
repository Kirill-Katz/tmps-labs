package com.example.tmps_lab.repository;

import com.example.tmps_lab.model.Account;
import java.util.UUID;

public interface AccountRepository {
    Account save(Account transaction);
    Account findById(UUID id);
}
