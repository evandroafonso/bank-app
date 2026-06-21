package com.assignment.bank.transaction.dto;

import java.math.BigDecimal;

public record TransactionRequest(
        String IBAN,
        BigDecimal amount,
        String description
) {
}
