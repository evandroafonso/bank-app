package com.assignment.bank.account.dto;

import com.assignment.bank.user.dto.UserResponse;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record AccountResponse(
        String uuid,
        String IBAN,
        String currency,
        BigDecimal balance,
        UserResponse user
) {
}
