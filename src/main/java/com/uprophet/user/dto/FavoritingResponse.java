package com.uprophet.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FavoritingResponse(
        @JsonProperty("to_user") String toUser,
        String currentUser,
        boolean didFavorite) {
}
