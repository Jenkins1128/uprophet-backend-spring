package com.uprophet.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SearchResultResponse(
        long id,
        @JsonProperty("user_name") String userName,
        String currentUser,
        boolean didFavorite) {
}
