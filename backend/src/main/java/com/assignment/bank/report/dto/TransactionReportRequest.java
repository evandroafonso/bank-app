package com.assignment.bank.report.dto;

import java.util.UUID;

public record TransactionReportRequest(
        UUID transactionUuid
) {
}