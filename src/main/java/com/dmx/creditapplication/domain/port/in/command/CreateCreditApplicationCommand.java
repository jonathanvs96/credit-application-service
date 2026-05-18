package com.dmx.creditapplication.domain.port.in.command;

import com.dmx.creditapplication.domain.model.enums.Currency;
import lombok.Builder;

import java.math.BigDecimal;

@Builder(toBuilder = true)
public record CreateCreditApplicationCommand(
    String customerName,
    String customerEmail,
    String customerRfc,
    BigDecimal requestedAmount,
    Currency currency,
    Integer termMonths,
    BigDecimal annualInterestRate
) {
}
