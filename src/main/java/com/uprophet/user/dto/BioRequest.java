package com.uprophet.user.dto;

import jakarta.validation.constraints.Size;

public record BioRequest(
        @Size(max = 1000, message = "Bio must be 1000 characters or less")
        String bio) {
}
