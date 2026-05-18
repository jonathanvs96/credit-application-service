package com.dmx.creditapplication.infrastructure.adapter.out.persistence.mapper;

import com.dmx.creditapplication.domain.model.entity.CreditApplication;
import com.dmx.creditapplication.domain.model.pagination.PageMetadata;
import com.dmx.creditapplication.domain.model.pagination.PageResult;
import com.dmx.creditapplication.domain.port.in.command.SearchCreditApplicationsQuery;
import com.dmx.creditapplication.infrastructure.adapter.out.persistence.entity.CreditApplicationEntity;
import com.dmx.creditapplication.infrastructure.adapter.out.persistence.querydsl.creditapplication.CreditApplicationSearchModel;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CreditApplicationPersistenceMapper {

  CreditApplicationEntity toEntity(CreditApplication creditApplication);

  CreditApplicationSearchModel toSearchModel(SearchCreditApplicationsQuery query);

  default CreditApplication toDomain(CreditApplicationEntity entity) {
    return CreditApplication.restore(
        entity.getId(),
        entity.getCustomerName(),
        entity.getCustomerEmail(),
        entity.getCustomerRfc(),
        entity.getRequestedAmount(),
        entity.getCurrency(),
        entity.getTermMonths(),
        entity.getAnnualInterestRate(),
        entity.getMonthlyPayment(),
        entity.getTotalToPay(),
        entity.getAmountUsd(),
        entity.getAmountEur(),
        entity.getExchangeRateDate(),
        entity.getStatus(),
        entity.getStatusReason(),
        entity.getCreatedAt(),
        entity.getUpdatedAt()
    );
  }

  default PageResult<CreditApplication> toPageResult(Page<CreditApplicationEntity> page) {
    List<CreditApplication> content = page.getContent()
        .stream()
        .map(this::toDomain)
        .toList();

    return PageResult.<CreditApplication>builder()
        .content(content)
        .metadata(new PageMetadata(
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages(),
            page.isLast()
        ))
        .build();
  }



}
