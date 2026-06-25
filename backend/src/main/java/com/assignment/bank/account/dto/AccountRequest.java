package com.assignment.bank.account.dto;

import com.assignment.bank.account.enums.Currency;
import jakarta.validation.constraints.NotNull;

public record AccountRequest(
        @NotNull
        @NotNull(message = "Currency is required")
        Currency currency
) {
}
