package com.uprophet.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PhotoRequest(
        @NotBlank(message = "File name is required")
        @Size(max = 100, message = "File name must be 100 characters or less")
        String name,
        @NotBlank(message = "Image data is required")
        String image) {
}
