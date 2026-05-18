package com.dmx.creditapplication.infrastructure.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "exchange.frankfurter")
public record FrankfurterProperties(
    String url,
    int connectTimeout,
    int readTimeout
) {
}
