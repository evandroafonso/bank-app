package com.assignment.bank.account.controller;

import com.assignment.bank.account.dto.AccountRequest;
import com.assignment.bank.account.entity.Account;
import com.assignment.bank.account.service.AccountService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping("/create")
    public ResponseEntity<Account> create(AccountRequest accountRequest) {
        accountService.save(accountRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}