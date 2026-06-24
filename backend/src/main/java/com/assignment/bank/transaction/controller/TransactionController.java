package com.assignment.bank.transaction.controller;

import com.assignment.bank.transaction.dto.BalanceChartPointResponse;
import com.assignment.bank.transaction.dto.TransactionRequest;
import com.assignment.bank.transaction.dto.TransactionResponse;
import com.assignment.bank.transaction.service.TransactionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

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

    @GetMapping("/history/{iban}")
    public ResponseEntity<Page<TransactionResponse>> getHistory(
            @PathVariable String iban,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(transactionService.getHistory(iban, PageRequest.of(page, size)));
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<TransactionResponse> getTransaction(@PathVariable UUID uuid) {
        return ResponseEntity.ok(transactionService.getTransaction(uuid));
    }

    @GetMapping("/balance-chart")
    public ResponseEntity<List<BalanceChartPointResponse>> getBalanceChartData(
            @RequestParam String iban,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<BalanceChartPointResponse> chartData = transactionService.getBalanceChartData(iban, startDate, endDate);
        return ResponseEntity.ok(chartData);
    }

}
