package com.uprophet.user.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record NotificationResponse(
        long id,
        String notice,
        Long quotesId,
        String date,
        String userName,
        String toUser,
        String type) {
}
