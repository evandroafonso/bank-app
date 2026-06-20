package com.assignment.bank.user.dto;

import lombok.Builder;

@Builder
public record UserResponse(
        String uuid,
        String username,
        String personalId,
        String email
) {
}
