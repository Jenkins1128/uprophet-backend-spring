package com.uprophet.quote.dto;

public record HomeQuoteResponse(
        long id,
        String userName,
        String title,
        String quote,
        String datePosted,
        long likeCount,
        boolean didLike) {
}
