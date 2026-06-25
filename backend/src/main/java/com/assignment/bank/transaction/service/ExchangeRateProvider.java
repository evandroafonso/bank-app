package com.assignment.bank.transaction.service;

import com.assignment.bank.account.enums.Currency;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

@Slf4j
@Component
public class ExchangeRateProvider {

    private static final Map<String, BigDecimal> RATES = Map.ofEntries(
            Map.entry("EUR_USD", new BigDecimal("1.15")),
            Map.entry("EUR_SEK", new BigDecimal("11.15")),
            Map.entry("EUR_GBP", new BigDecimal("0.85")),
            Map.entry("EUR_VND", new BigDecimal("27100.00")),

            Map.entry("USD_EUR", new BigDecimal("0.87")),
            Map.entry("USD_SEK", new BigDecimal("10.32")),
            Map.entry("USD_GBP", new BigDecimal("0.79")),
            Map.entry("USD_VND", new BigDecimal("25100.00")),

            Map.entry("SEK_EUR", new BigDecimal("0.090")),
            Map.entry("SEK_USD", new BigDecimal("0.097")),
            Map.entry("SEK_GBP", new BigDecimal("0.076")),
            Map.entry("SEK_VND", new BigDecimal("2430.00")),

            Map.entry("GBP_EUR", new BigDecimal("1.18")),
            Map.entry("GBP_USD", new BigDecimal("1.27")),
            Map.entry("GBP_SEK", new BigDecimal("13.13")),
            Map.entry("GBP_VND", new BigDecimal("31950.00")),

            Map.entry("VND_EUR", new BigDecimal("0.000037")),
            Map.entry("VND_USD", new BigDecimal("0.000040")),
            Map.entry("VND_SEK", new BigDecimal("0.000412")),
            Map.entry("VND_GBP", new BigDecimal("0.000031"))
    );

    public BigDecimal getRate(Currency source, Currency target) {
        log.debug("Fetching exchange rate for conversion: {} -> {}", source, target);

        if (source == target) {
            log.debug("Source and target currencies are the same ({}). Rate is 1", source);
            return BigDecimal.ONE;
        }

        String key = source + "_" + target;
        BigDecimal rate = RATES.get(key);

        if (rate == null) {
            log.warn("Exchange rate conversion not supported: {} -> {}", source, target);
            throw new IllegalArgumentException("Unsupported conversion: " + source + " -> " + target);
        }

        log.debug("Exchange rate for {} -> {} is {}", source, target, rate);
        return rate;
    }
}