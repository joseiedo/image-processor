package br.com.imageprocessor.infra.ratelimit;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.ratelimit")
public record RateLimitProperties(boolean enabled, int maxRequestsPerWindow, int windowSeconds) {
}
