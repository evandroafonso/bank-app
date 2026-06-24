package com.assignment.bank.transaction.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BalanceChartPointResponse(
        LocalDateTime timestamp,
        BigDecimal balance
) {
}
