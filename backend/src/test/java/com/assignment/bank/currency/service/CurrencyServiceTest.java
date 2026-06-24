package com.assignment.bank.currency.service;

import com.assignment.bank.account.enums.Currency;
import com.assignment.bank.currency.dto.CurrencyResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CurrencyServiceTest {

    private final CurrencyService service = new CurrencyService();

    @Test
    void shouldReturnAllCurrencies() {

        List<CurrencyResponse> result = service.getCurrencies();

        assertThat(result)
                .isNotNull()
                .hasSize(Currency.values().length);

        for (Currency currency : Currency.values()) {

            assertThat(result)
                    .anySatisfy(response -> {
                        assertThat(response.code())
                                .isEqualTo(currency.name());

                        assertThat(response.description())
                                .isEqualTo(currency.getDescription());
                    });
        }
    }

    @Test
    void shouldReturnCurrenciesInEnumOrder() {

        List<CurrencyResponse> result = service.getCurrencies();

        Currency[] currencies = Currency.values();

        for (int i = 0; i < currencies.length; i++) {

            assertThat(result.get(i).code())
                    .isEqualTo(currencies[i].name());

            assertThat(result.get(i).description())
                    .isEqualTo(currencies[i].getDescription());
        }
    }

    @Test
    void shouldNotReturnEmptyList() {

        List<CurrencyResponse> result = service.getCurrencies();

        assertThat(result).isNotEmpty();
    }
}