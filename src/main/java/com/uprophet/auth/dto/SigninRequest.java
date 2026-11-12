package com.uprophet.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SigninRequest(
        @NotBlank(message = "Username is required")
        @Size(max = 20, message = "Username must be 20 characters or less")
        String username,
        @NotBlank(message = "Password is required")
        @Size(max = 128, message = "Password must be 128 characters or less")
        String password) {
}
