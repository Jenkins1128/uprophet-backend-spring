package com.uprophet.auth.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;

import org.junit.jupiter.api.Test;

import com.uprophet.config.AppProperties;

class JwtServiceTest {

    private static final String ACCESS_SECRET = "access-secret-access-secret-access-secret";
    private static final String NODE_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6NDIsInVzZXJuYW1lIjoiYWRhIiwiaWF0IjoxNzk0NDY4Mzg4LCJleHAiOjIxMDk4MjgzODh9.Rwzdv3BB90_x1_kw4i6ErfHXJEkkXKOZyd1mHYSovPo";

    @Test
    void parsesNodeLifetimes() {
        assertThat(TokenLifetimes.parse("120s", Duration.ofHours(1))).isEqualTo(Duration.ofSeconds(120));
        assertThat(TokenLifetimes.parse("365d", Duration.ofDays(1))).isEqualTo(Duration.ofDays(365));
        assertThat(TokenLifetimes.parse(null, Duration.ofHours(1))).isEqualTo(Duration.ofHours(1));
    }

    @Test
    void verifiesATokenSignedByNodeJsonwebtoken() {
        AuthUser user = service().verifyAccess(NODE_TOKEN);

        assertThat(user.id()).isEqualTo(42);
        assertThat(user.username()).isEqualTo("ada");
    }

    @Test
    void roundTripsAccessTokens() {
        JwtService service = service();

        AuthUser user = service.verifyAccess(service.signAccess(7, "ada"));

        assertThat(user).isEqualTo(new AuthUser(7, "ada"));
    }

    private static JwtService service() {
        return new JwtService(new AppProperties(
                new AppProperties.Resend(""),
                new AppProperties.Cors(""),
                new AppProperties.Jwt(ACCESS_SECRET, "120s", "refresh-secret-refresh-secret-refresh-secret", "365d"),
                new AppProperties.Password("site-key", "salt")));
    }
}
