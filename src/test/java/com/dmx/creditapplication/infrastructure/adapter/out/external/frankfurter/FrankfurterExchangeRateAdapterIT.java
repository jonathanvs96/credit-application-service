package com.dmx.creditapplication.infrastructure.adapter.out.external.frankfurter;

import com.dmx.creditapplication.domain.model.entity.ExchangeRates;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.math.BigDecimal;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@WireMockTest
public class FrankfurterExchangeRateAdapterIT {

  private static WireMockRuntimeInfo wmRuntimeInfo;

  @Autowired
  private FrankfurterExchangeRateAdapter adapter;
  @Autowired
  private CacheManager cacheManager;

  @BeforeAll
  static void init(WireMockRuntimeInfo runtimeInfo) {
    wmRuntimeInfo = runtimeInfo;
  }

  @DynamicPropertySource
  static void registerProperties(DynamicPropertyRegistry registry) {
    registry.add(
        "exchange.frankfurter.url",
        () -> wmRuntimeInfo.getHttpBaseUrl() + "/v1"
    );
  }

  @BeforeEach
  void setUp() {
    resetAllRequests();
    Cache cache = cacheManager.getCache("exchangeRates");
    if (cache != null) {
      cache.clear();
    }
  }

  @Test
  void should_return_exchange_rates_successfully() {
    // Arrange
    successStub();

    // Act
    ExchangeRates result = adapter.getRate(
        "MXN",
        "USD",
        "EUR"
    );

    // Assert

    assertNotNull(result);
    assertAll(
        () -> assertThat(result.baseCurrency()).isEqualTo("MXN"),
        () -> assertThat(result.date()).isEqualTo("2026-05-18"),
        () -> assertThat(result.rates())
            .containsEntry("EUR", new BigDecimal("0.04965"))
            .containsEntry("USD", new BigDecimal("0.05783"))
    );

    verify(1, getRequestedFor(urlPathEqualTo("/v1/latest")));
  }

  @Test
  void should_return_fallback_when_api_fails() {
    // Arrange
    stubFor(get(urlPathEqualTo("/v1/latest"))
        .withQueryParam("base", equalTo("MXN"))
        .withQueryParam("symbols", equalTo("USD,EUR"))
        .willReturn(serverError()));

    // Act
    ExchangeRates result = adapter.getRate(
        "MXN",
        "USD",
        "EUR"
    );

    // Assert
    assertNotNull(result);
    assertAll(
        () -> assertNull(result.date()),
        () -> assertThat(result.rates()).isEmpty(),
        () -> assertThat(result.baseCurrency()).isEqualTo("MXN")
    );

    verify(1, getRequestedFor(urlPathEqualTo("/v1/latest")));
  }

  @Test
  void should_return_fallback_when_timeout_occurs() {
    // Assert
    stubFor(get(urlPathEqualTo("/v1/latest"))
        .willReturn(aResponse()
            .withFixedDelay(3000)));

    // Act
    ExchangeRates result = adapter.getRate(
        "MXN",
        "USD",
        "EUR"
    );

    // Assert
    assertNotNull(result);
    assertAll(
        () -> assertNull(result.date()),
        () -> assertThat(result.rates()).isEmpty(),
        () -> assertThat(result.baseCurrency()).isEqualTo("MXN")
    );

    verify(1, getRequestedFor(urlPathEqualTo("/v1/latest")));
  }

  @Test
  void should_cache_exchange_rates_and_call_api_only_once() {
    // Arrange
    successStub();

    // Act
    // First call for cache
    adapter.getRate("MXN", "USD", "EUR");

    // Second call from cache
    adapter.getRate("MXN", "USD", "EUR");

    verify(1, getRequestedFor(urlPathEqualTo("/v1/latest")));
  }

  private void successStub() {
    stubFor(get(urlPathEqualTo("/v1/latest"))
        .withQueryParam("base", equalTo("MXN"))
        .withQueryParam("symbols", equalTo("USD,EUR"))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody("""
              {
                "amount": 1.0,
                "base": "MXN",
                "date": "2026-05-18",
                "rates": {
                    "EUR": 0.04965,
                    "USD": 0.05783
                }
              }
              """)));
  }


}
