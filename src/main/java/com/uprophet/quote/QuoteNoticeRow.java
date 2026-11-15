package com.uprophet.quote;

import java.time.LocalDateTime;

public record QuoteNoticeRow(Long id, String notice, Long quotesId, LocalDateTime date, String userName) {
}
