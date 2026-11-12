package com.uprophet.auth.security;

import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TokenLifetimes {

    private static final Pattern LIFE = Pattern.compile("^(\\d+)\\s*([smhd])$", Pattern.CASE_INSENSITIVE);

    private TokenLifetimes() {
    }

    public static Duration parse(String value, Duration fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        Matcher matcher = LIFE.matcher(value.trim());
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Unsupported token lifetime: " + value);
        }
        long amount = Long.parseLong(matcher.group(1));
        return switch (matcher.group(2).toLowerCase()) {
            case "s" -> Duration.ofSeconds(amount);
            case "m" -> Duration.ofMinutes(amount);
            case "h" -> Duration.ofHours(amount);
            case "d" -> Duration.ofDays(amount);
            default -> throw new IllegalArgumentException("Unsupported token lifetime: " + value);
        };
    }
}
