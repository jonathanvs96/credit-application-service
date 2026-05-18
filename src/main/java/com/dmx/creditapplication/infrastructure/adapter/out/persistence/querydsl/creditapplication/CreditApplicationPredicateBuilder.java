package com.dmx.creditapplication.infrastructure.adapter.out.persistence.querydsl.creditapplication;

import com.dmx.creditapplication.infrastructure.adapter.out.persistence.entity.QCreditApplicationEntity;
import com.querydsl.core.BooleanBuilder;
import org.springframework.stereotype.Component;

@Component
public class CreditApplicationPredicateBuilder {

  private final QCreditApplicationEntity credit = QCreditApplicationEntity.creditApplicationEntity;

  public BooleanBuilder build(CreditApplicationSearchModel query){
    BooleanBuilder builder = new BooleanBuilder();

    if (query.customerRfc() != null) {
      builder.and(credit.customerRfc.eq(query.customerRfc()));
    }

    if (query.status() != null) {
      builder.and(credit.status.eq(query.status()));
    }

    if (query.minAmount() != null) {
      builder.and(credit.requestedAmount.goe(query.minAmount()));
    }

    if (query.maxAmount() != null) {
      builder.and(credit.requestedAmount.loe(query.maxAmount()));
    }

    return builder;
  }

}
