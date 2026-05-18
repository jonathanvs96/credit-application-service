package com.dmx.creditapplication.domain.port.in.command;

import com.dmx.creditapplication.domain.model.enums.CreditStatus;

import java.math.BigDecimal;

public record SearchCreditApplicationsQuery(
    Integer page,
    Integer size,

    String customerRfc,
    CreditStatus status,

    BigDecimal minAmount,
    BigDecimal maxAmount
) {
}
