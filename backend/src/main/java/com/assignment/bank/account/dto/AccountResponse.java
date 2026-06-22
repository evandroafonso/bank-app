package com.assignment.bank.account.dto;

import com.assignment.bank.user.dto.UserResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AccountResponse(
        String uuid,
        String iban,
        String currency,
        BigDecimal balance,
        UserResponse user
) {
}
