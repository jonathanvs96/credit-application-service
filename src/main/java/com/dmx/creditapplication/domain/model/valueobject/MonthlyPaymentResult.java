package com.dmx.creditapplication.domain.model.valueobject;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record MonthlyPaymentResult(
    BigDecimal monthlyPayment,
    BigDecimal totalToPay
) {
}
