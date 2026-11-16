package com.uprophet.quote.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record QuoteIdRequest(
        @NotNull(message = "Quote is required")
        @Positive(message = "Quote is required")
        Long quoteId) {
}
