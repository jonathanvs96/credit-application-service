package com.dmx.creditapplication.infrastructure.adapter.out.external.frankfurter;

import com.dmx.creditapplication.domain.port.out.ExchangeRateProvider;
import com.dmx.creditapplication.domain.model.entity.ExchangeRates;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Arrays;
import java.util.Collections;

@Component
@RequiredArgsConstructor
@Slf4j
public class FrankfurterExchangeRateAdapter implements ExchangeRateProvider {

  private final RestClient frankfurterRestClient;

  @Override
  @Retry(name = "frankfurter")
  @CircuitBreaker(
      name = "frankfurter",
      fallbackMethod = "fallbackRate")
  @Cacheable(
      value = "exchangeRates",
      key = "T(com.dmx.creditapplication.infrastructure.config.cache.CacheKeys)" +
          ".exchangeRateKey(#baseCurrency, #targetCurrencies)"
  )
  public ExchangeRates getRate(String baseCurrency, String ...targetCurrencies) {
    String symbols = String.join(",", targetCurrencies);
    FrankfurterResponse response = frankfurterRestClient.get()
        .uri(uriBuilder -> uriBuilder
            .path("/latest")
            .queryParam("base", baseCurrency)
            .queryParam("symbols", symbols)
            .build()
        )
        .retrieve()
        .body(FrankfurterResponse.class);
    return new ExchangeRates(response.base(), response.date(), response.rates());
  }

  public ExchangeRates fallbackRate(
      String baseCurrency,
      String[] targetCurrencies,
      Throwable throwable
  ) {
    log.warn(
        "Frankfurter unavailable. " +
            "Returning empty exchange rates. " +
            "baseCurrency={}, targetCurrencies={}",
        baseCurrency,
        Arrays.toString(targetCurrencies),
        throwable
    );
    return new ExchangeRates(baseCurrency, null, Collections.emptyMap());
  }
}
