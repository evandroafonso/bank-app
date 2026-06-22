package com.assignment.bank.transaction.dto;

import com.assignment.bank.account.enums.Currency;
import com.assignment.bank.transaction.enums.TransactionType;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record TransactionResponse(
        String uuid,
        BigDecimal sourceAmount,
        BigDecimal convertedAmount,
        BigDecimal exchangeRate,
        Currency currency,
        Currency targetCurrency,
        BigDecimal balance,
        TransactionType type,
        String description,
        LocalDateTime timestamp
) {
}