package com.dmx.creditapplication.infrastructure.adapter.out.external.frankfurter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

public record FrankfurterResponse(
    String base,
    LocalDate date,
    Map<String, BigDecimal> rates
) {
}


