package com.assignment.bank.currency.service;

import com.assignment.bank.account.enums.Currency;
import com.assignment.bank.currency.dto.CurrencyResponse;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class CurrencyService {

    public List<CurrencyResponse> getCurrencies() {
        return Arrays.stream(Currency.values())
                .map(currency -> new CurrencyResponse(
                        currency.name(),
                        currency.getDescription()))
                .toList();
    }
}
