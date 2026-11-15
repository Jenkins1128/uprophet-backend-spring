package com.uprophet.quote.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record AddCommentRequest(
        @NotNull(message = "Quote is required")
        @Positive(message = "Quote is required")
        Long quoteId,
        @NotBlank(message = "Comment is required")
        @Size(max = 255, message = "Comment must be 255 characters or less")
        String comment) {
}
