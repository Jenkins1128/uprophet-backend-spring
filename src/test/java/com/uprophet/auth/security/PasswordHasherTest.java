package com.uprophet.auth.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.uprophet.config.AppProperties;

class PasswordHasherTest {

    @Test
    void matchesTheNodeMd5AndHmacSha512Hash() {
        PasswordHasher hasher = new PasswordHasher(properties("site-key", "salt"));

        String hash = hasher.hash("ada", "secret1", 1_700_000_000_000L);

        assertThat(hash).isEqualTo(
                "23b201599075647eb65ff3f2a102060d836b651c4341604ea3701466568c74fc74ce0440def1b267a6cf2d68a8e140546197cc8a60c2f87ca36350eccc58cdab");
        assertThat(hasher.matches("ada", "secret1", hash, 1_700_000_000_000L)).isTrue();
        assertThat(hasher.matches("ada", "wrong", hash, 1_700_000_000_000L)).isFalse();
    }

    private static AppProperties properties(String siteKey, String nonceSalt) {
        return new AppProperties(
                new AppProperties.Resend(""),
                new AppProperties.Cors(""),
                new AppProperties.Jwt("access-secret-access-secret-access-secret", "1h", "refresh-secret-refresh-secret-refresh-secret", "7d"),
                new AppProperties.Password(siteKey, nonceSalt));
    }
}
