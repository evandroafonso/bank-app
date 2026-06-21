package com.assignment.bank.transaction.dto;

import com.assignment.bank.account.enums.Currency;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record TransactionResponse(
        String transactionUuid,
        BigDecimal amount,
        BigDecimal balance,
        Currency currency,
        String description,
        LocalDateTime timestamp
) {
}