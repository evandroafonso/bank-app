package com.assignment.bank.transaction.dto;

import com.assignment.bank.account.enums.Currency;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record TransactionRequest(
        String IBAN,
        
        @NotNull
        @Positive
        @Digits(integer = 15, fraction = 4)
        BigDecimal amount,
        String description,
        Currency currency
) {
}
