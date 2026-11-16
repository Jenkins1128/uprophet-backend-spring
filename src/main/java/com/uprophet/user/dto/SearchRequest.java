package com.uprophet.user.dto;

import jakarta.validation.constraints.NotBlank;

public record SearchRequest(
        @NotBlank(message = "Search is required")
        String search) {
}
