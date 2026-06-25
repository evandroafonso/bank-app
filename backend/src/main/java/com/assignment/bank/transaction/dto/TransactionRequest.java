package com.assignment.bank.transaction.dto;

import com.assignment.bank.account.enums.Currency;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record TransactionRequest(

        @NotBlank(message = "IBAN is required")
        @Size(max = 34, message = "IBAN cannot exceed 34 characters")
        String iban,

        @NotNull(message = "Transaction amount is required")
        @Positive(message = "Transaction amount must be greater than zero")
        @Digits(
                integer = 12,
                fraction = 8,
                message = "Transaction amount must have up to 20 integer digits and 8 decimal places"
        )
        BigDecimal amount,

        @NotNull
        @NotNull(message = "Currency is required")
        Currency currency,

        @Size(max = 255, message = "Description cannot exceed 255 characters")
        String description
) {
}
