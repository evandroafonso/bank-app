package com.assignment.bank.report.controller;

import com.assignment.bank.report.dto.TransactionReportRequest;
import com.assignment.bank.report.dto.TransactionReportResponse;
import com.assignment.bank.report.service.TransactionReportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
public class TransactionReportController {

    private final TransactionReportService service;

    public TransactionReportController(TransactionReportService service) {
        this.service = service;
    }

    @PostMapping("/transaction/pdf")
    public ResponseEntity<byte[]> generate(@RequestBody TransactionReportRequest request) {

        TransactionReportResponse response = service.generate(request);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=" + response.fileName())
                .contentType(MediaType.APPLICATION_PDF)
                .body(response.pdf());
    }
}