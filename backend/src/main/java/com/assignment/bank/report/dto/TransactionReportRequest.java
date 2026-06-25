package com.assignment.bank.report.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record TransactionReportRequest(
        @NotNull(message = "Transaction UUID is required")
        UUID transactionUuid
) {
}