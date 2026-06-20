package com.assignment.bank.exception.model;

public record FieldErrorResponse(
        String field,
        String message
) {
}