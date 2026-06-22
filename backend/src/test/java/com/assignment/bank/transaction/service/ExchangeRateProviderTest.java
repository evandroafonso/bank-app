package com.assignment.bank.transaction.service;

import com.assignment.bank.account.enums.Currency;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ExchangeRateProviderTest {

    private final ExchangeRateProvider exchangeRateProvider = new ExchangeRateProvider();

    @Test
    void shouldReturnOneWhenSourceAndTargetCurrenciesAreEqual() {
        BigDecimal rate = exchangeRateProvider.getRate(Currency.EUR, Currency.EUR);

        assertEquals(0, BigDecimal.ONE.compareTo(rate));
    }

    @Test
    void shouldReturnUsdToEurRate() {
        BigDecimal rate = exchangeRateProvider.getRate(Currency.EUR, Currency.USD);
        assertEquals(0, new BigDecimal("1.17").compareTo(rate));
    }

    @Test
    void shouldReturnEurToUsdRate() {
        BigDecimal rate = exchangeRateProvider.getRate(Currency.USD, Currency.EUR);
        assertEquals(0, new BigDecimal("0.85").compareTo(rate));
    }

    @Test
    void shouldThrowExceptionWhenConversionFromEurToUnsupportedCurrency() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> exchangeRateProvider.getRate(Currency.EUR, null)
        );
        assertEquals("Unsupported conversion: EUR -> null", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenConversionFromUsdToUnsupportedCurrency() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> exchangeRateProvider.getRate(Currency.USD, null)
        );
        assertEquals("Unsupported conversion: USD -> null", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenSourceCurrencyIsNull() {
        assertThrows(
                NullPointerException.class,
                () -> exchangeRateProvider.getRate(null, Currency.EUR)
        );
    }
}