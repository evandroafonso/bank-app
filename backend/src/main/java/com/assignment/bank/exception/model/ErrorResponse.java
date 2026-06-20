package com.assignment.bank.exception.model;

import java.time.LocalDateTime;

public record ErrorResponse(
        String message,
        String errorCode,
        int status,
        LocalDateTime timestamp
) {
}