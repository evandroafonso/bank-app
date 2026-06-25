package com.assignment.bank.currency.service;

import com.assignment.bank.account.enums.Currency;
import com.assignment.bank.currency.dto.CurrencyResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class CurrencyService {

    public List<CurrencyResponse> getCurrencies() {
        log.info("Fetching all available system currencies from enum");

        return Arrays.stream(Currency.values())
                .map(currency -> new CurrencyResponse(
                        currency.name(),
                        currency.getDescription()))
                .toList();
    }
}