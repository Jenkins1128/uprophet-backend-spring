package com.uprophet.auth.security;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Component;

import com.uprophet.config.AppProperties;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@Component
public class JwtService {

    private final AppProperties properties;
    private final JsonMapper jsonMapper = JsonMapper.shared();

    public JwtService(AppProperties properties) {
        this.properties = properties;
    }

    public String signAccess(long id, String username) {
        Duration life = TokenLifetimes.parse(properties.jwt().accessTokenLife(), Duration.ofHours(1));
        return sign(id, username, life, accessKey());
    }

    public String signRefresh(long id, String username) {
        Duration life = TokenLifetimes.parse(properties.jwt().refreshTokenLife(), Duration.ofDays(7));
        return sign(id, username, life, refreshKey());
    }

    public AuthUser verifyAccess(String token) {
        return readVerified(token, accessKey());
    }

    public AuthUser verifyRefresh(String token) {
        return readVerified(token, refreshKey());
    }

    public AuthUser readUnverified(String token) {
        String[] parts = token.split("\\.");
        if (parts.length < 2) {
            throw new IllegalArgumentException("Access token payload is missing");
        }
        byte[] decoded = Base64.getUrlDecoder().decode(pad(parts[1]));
        JsonNode payload = jsonMapper.readTree(new String(decoded, StandardCharsets.UTF_8));
        JsonNode id = payload.get("id");
        JsonNode username = payload.get("username");
        if (id == null || !id.isNumber() || username == null || !username.isString()) {
            throw new IllegalArgumentException("Access token payload is incomplete");
        }
        return new AuthUser(id.asLong(), username.asString());
    }

    private String sign(long id, String username, Duration life, SecretKey key) {
        Instant now = Instant.now();
        return Jwts.builder()
                .claim("id", id)
                .claim("username", username)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(life)))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    private AuthUser readVerified(String token, SecretKey key) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        Object id = claims.get("id");
        Object username = claims.get("username");
        if (!(id instanceof Number) || !(username instanceof String)) {
            throw new IllegalArgumentException("Token payload is incomplete");
        }
        return new AuthUser(((Number) id).longValue(), (String) username);
    }

    private SecretKey accessKey() {
        return hmacKey(properties.jwt().accessTokenSecret());
    }

    private SecretKey refreshKey() {
        return hmacKey(properties.jwt().refreshTokenSecret());
    }

    private static SecretKey hmacKey(String secret) {
        return new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    private static String pad(String value) {
        int remainder = value.length() % 4;
        if (remainder == 0) {
            return value;
        }
        return value + "=".repeat(4 - remainder);
    }
}
