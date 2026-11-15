package com.uprophet.quote.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateQuoteRequest(
        @NotBlank(message = "Title is required")
        @Size(max = 20, message = "Title must be 20 characters or less")
        String title,
        @NotBlank(message = "Quote is required")
        @Size(max = 500, message = "Quote must be 500 characters or less")
        String quote) {
}
