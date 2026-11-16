package com.uprophet.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {

    @Bean
    CorsConfigurationSource corsConfigurationSource(AppProperties properties) {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowCredentials(true);
        configuration.setAllowedOriginPatterns(originPatterns(properties));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    private static List<String> originPatterns(AppProperties properties) {
        List<String> patterns = new ArrayList<>(List.of(
                "http://localhost",
                "http://localhost:*",
                "http://uprophet.com",
                "http://uprophet.com:*",
                "https://uprophet.com",
                "https://uprophet.com:*",
                "http://*.uprophet.com",
                "http://*.uprophet.com:*",
                "https://*.uprophet.com",
                "https://*.uprophet.com:*"));
        String configured = properties.cors().allowedOrigins();
        if (configured != null && !configured.isBlank()) {
            for (String origin : configured.split(",")) {
                String trimmed = origin.trim();
                if (!trimmed.isEmpty() && !patterns.contains(trimmed)) {
                    patterns.add(trimmed);
                }
            }
        }
        return patterns;
    }
}
