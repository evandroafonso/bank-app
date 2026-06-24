package com.assignment.bank.account.enums;

public enum Currency {

    EUR("Euro"),
    USD("US Dollar"),
    SEK("Swedish Krona"),
    GBP("British Pound"),
    VND("Vietnamese Dong");

    private final String description;

    Currency(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}