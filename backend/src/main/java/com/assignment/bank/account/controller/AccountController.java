package com.assignment.bank.account.controller;

import com.assignment.bank.account.dto.AccountRequest;
import com.assignment.bank.account.dto.AccountResponse;
import com.assignment.bank.account.service.AccountService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping("/create")
    public ResponseEntity<AccountResponse> create(@RequestBody AccountRequest accountRequest) {
        AccountResponse savedAccount = accountService.save(accountRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedAccount);
    }

    @GetMapping()
    public ResponseEntity<List<AccountResponse>> findAll() {
        List<AccountResponse> accounts = accountService.findAll();
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/{IBAN}")
    public ResponseEntity<AccountResponse> findByIBAN(@PathVariable String IBAN) {
        AccountResponse account = accountService.findByIBAN(IBAN);
        return ResponseEntity.ok(account);
    }

}