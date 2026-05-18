package com.dmx.creditapplication.infrastructure.adapter.out.persistence.querydsl.creditapplication;

import com.dmx.creditapplication.domain.model.enums.CreditStatus;

import java.math.BigDecimal;

public record CreditApplicationSearchModel(
    Integer page,
    Integer size,

    String customerRfc,
    CreditStatus status,

    BigDecimal minAmount,
    BigDecimal maxAmount
) {}
