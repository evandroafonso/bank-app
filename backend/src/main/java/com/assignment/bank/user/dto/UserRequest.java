package com.assignment.bank.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record UserRequest(
        @NotBlank
        String username,

        @NotBlank
        String personalId,

        @Email
        @NotBlank
        String email,

        @Size(min = 6, max = 20)
        String password
) {
}