package com.dmx.creditapplication.domain.model.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

public record ExchangeRates(
    String baseCurrency,
    LocalDate date,
    Map<String, BigDecimal> rates
) {
}
