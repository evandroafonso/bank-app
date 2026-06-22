package com.assignment.bank.account.dto;

import java.math.BigDecimal;

public record AccountBalanceResponse(
        String iban,
        String currency,
        BigDecimal balance
) {
}
