package com.assignment.bank.currency.controller;

import com.assignment.bank.currency.dto.CurrencyResponse;
import com.assignment.bank.currency.service.CurrencyService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CurrencyControllerTest {

    @Mock
    private CurrencyService currencyService;

    @InjectMocks
    private CurrencyController controller;

    @Test
    void shouldReturnCurrencies() {

        List<CurrencyResponse> expected = List.of(
                new CurrencyResponse("USD", "American Dollar"),
                new CurrencyResponse("EUR", "Euro")
        );

        when(currencyService.getCurrencies())
                .thenReturn(expected);

        List<CurrencyResponse> result =
                controller.getCurrencies();

        assertThat(result)
                .isNotNull()
                .hasSize(2)
                .isEqualTo(expected);

        verify(currencyService).getCurrencies();
        verifyNoMoreInteractions(currencyService);
    }

    @Test
    void shouldReturnEmptyListWhenNoCurrenciesExist() {

        when(currencyService.getCurrencies())
                .thenReturn(List.of());

        List<CurrencyResponse> result =
                controller.getCurrencies();

        assertThat(result).isEmpty();

        verify(currencyService).getCurrencies();
        verifyNoMoreInteractions(currencyService);
    }
}