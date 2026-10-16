package com.example.tmps_lab.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.tmps_lab.service.AccountService;
import com.example.tmps_lab.model.Account;

import java.util.UUID;

@RestController
@RequestMapping("/accounts")
public class AccountController {
    private final AccountService service;

    public AccountController(AccountService service) {
        this.service = service;
    }

    @PostMapping
    public Account create() {
        return service.create();
    }

    @GetMapping("/{id}")
    public Account get(@PathVariable UUID id) {
        return service.get(id);
    }
}
