package com.assignment.bank.transaction.service;

import com.assignment.bank.account.enums.Currency;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class ExchangeRateProvider {

    public static final BigDecimal EUR_TO_USD = new BigDecimal("0.85");
    public static final BigDecimal USD_TO_EUR = new BigDecimal("1.17");

    public BigDecimal getRate(Currency source, Currency target) { // <- remove static
        if (source == target) {
            return BigDecimal.ONE;
        }

        return switch (source) {
            case EUR -> target == Currency.USD ? USD_TO_EUR : throwUnsupported(source, target);
            case USD -> target == Currency.EUR ? EUR_TO_USD : throwUnsupported(source, target);
            default -> throwUnsupported(source, target);
        };
    }

    private BigDecimal throwUnsupported(Currency source, Currency target) { // <- remove static
        throw new IllegalArgumentException("Unsupported conversion: " + source + " -> " + target);
    }
}