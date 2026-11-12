package com.uprophet.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ForgotPasswordRequest(
        @NotBlank(message = "Username is required")
        @Size(max = 20, message = "Username must be 20 characters or less")
        String username,
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email address")
        @Size(max = 100, message = "Email must be 100 characters or less")
        String email) {
}
