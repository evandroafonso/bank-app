package com.assignment.bank.login.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "Email must not be blank")
        @Email(message = "Email must be a well-formed email address")
        @Size(max = 254, message = "Email must not exceed 254 characters")
        String email,

        @NotBlank(message = "Password must not be blank")
        @Size(min = 6, max = 128, message = "Password must be between 6 and 128 characters")
        String password
) {
}