package com.uprophet.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignupRequest(
        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
        String username,
        @NotBlank(message = "Name is required")
        @Size(max = 20, message = "Name must be 20 characters or less")
        String name,
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email address")
        @Size(max = 100, message = "Email must be 100 characters or less")
        String email,
        @NotBlank(message = "Password is required")
        @Size(min = 6, max = 128, message = "Password must be between 6 and 128 characters")
        String password) {
}
