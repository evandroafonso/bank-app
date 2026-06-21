package com.assignment.bank.account.dto;

import com.assignment.bank.account.enums.Currency;

public record AccountRequest(Currency currency) {
}
