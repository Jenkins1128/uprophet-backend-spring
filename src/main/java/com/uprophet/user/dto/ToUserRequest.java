package com.uprophet.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ToUserRequest(
        @NotBlank(message = "Username is required")
        @Size(max = 20, message = "Username must be 20 characters or less")
        String toUser) {
}
