package com.assignment.bank.transaction.controller;

import com.assignment.bank.transaction.dto.TransactionRequest;
import com.assignment.bank.transaction.dto.TransactionResponse;
import com.assignment.bank.transaction.service.TransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/credit")
    public ResponseEntity<TransactionResponse> credit(@RequestBody TransactionRequest transactionRequest) {
        TransactionResponse response = transactionService.credit(transactionRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/debit")
    public ResponseEntity<TransactionResponse> debit(@RequestBody TransactionRequest transactionRequest) {
        TransactionResponse response = transactionService.debit(transactionRequest);
        return ResponseEntity.ok(response);
    }

}
