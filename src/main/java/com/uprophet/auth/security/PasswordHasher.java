package com.uprophet.auth.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Component;

import com.uprophet.config.AppProperties;

@Component
public class PasswordHasher {

    private final AppProperties properties;
    private final SecureRandom random = new SecureRandom();

    public PasswordHasher(AppProperties properties) {
        this.properties = properties;
    }

    public String hash(String username, String password, long userRegistered) {
        String nonce = md5Hex("registration-" + username + userRegistered + properties.password().nonceSalt());
        return hmacSha512Hex(properties.password().siteKey(), password + nonce);
    }

    public boolean matches(String username, String password, String storedHash, long userRegistered) {
        return hash(username, password, userRegistered).equals(storedHash);
    }

    public String temporaryPassword() {
        byte[] bytes = new byte[7];
        random.nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes).substring(0, 7);
    }

    private static String md5Hex(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("MD5 is not available", exception);
        }
    }

    private static String hmacSha512Hex(String key, String value) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
            return HexFormat.of().formatHex(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("HMAC-SHA512 is not available", exception);
        }
    }
}
