package com.example.tmps_lab.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.tmps_lab.service.BankingFacade;
import com.example.tmps_lab.model.Account;

import java.util.UUID;

@RestController
@RequestMapping("/accounts")
public class AccountController {
    private final BankingFacade banking;

    public AccountController(BankingFacade banking) {
        this.banking = banking;
    }

    @PostMapping
    public Account create() {
        return banking.createAccount();
    }

    @GetMapping("/{id}")
    public Account get(@PathVariable UUID id) {
        return banking.getAccount(id);
    }
}
