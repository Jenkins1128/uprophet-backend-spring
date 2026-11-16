package com.uprophet.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(Resend resend, Cors cors, Jwt jwt, Password password) {

    public record Resend(String apiKey) {
    }

    public record Cors(String allowedOrigins) {
    }

    public record Jwt(
            String accessTokenSecret,
            String accessTokenLife,
            String refreshTokenSecret,
            String refreshTokenLife) {
    }

    public record Password(String siteKey, String nonceSalt) {
    }
}
