package com.uprophet.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FavoriterResponse(
        @JsonProperty("from_user") String fromUser,
        String currentUser,
        boolean didFavorite) {
}
