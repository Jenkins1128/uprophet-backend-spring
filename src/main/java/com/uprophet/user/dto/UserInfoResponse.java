package com.uprophet.user.dto;

public record UserInfoResponse(
        String currentUser,
        boolean didFavorite,
        long favoriters,
        long favoriting,
        String bio) {
}
