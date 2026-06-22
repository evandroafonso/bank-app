package com.assignment.bank.report.dto;

public record TransactionReportResponse(
        byte[] pdf,
        String fileName
) {
}