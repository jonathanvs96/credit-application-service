package com.dmx.creditapplication.infrastructure.adapter.in.web.mapper;

import com.dmx.creditapplication.domain.model.entity.CreditApplication;
import com.dmx.creditapplication.domain.model.pagination.PageResult;
import com.dmx.creditapplication.domain.model.enums.Currency;
import com.dmx.creditapplication.domain.port.in.command.ChangeCreditApplicationStatusCommand;
import com.dmx.creditapplication.domain.port.in.command.CreateCreditApplicationCommand;
import com.dmx.creditapplication.domain.port.in.command.SearchCreditApplicationsQuery;
import com.dmx.creditapplication.infrastructure.adapter.in.web.dto.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface CreditApplicationWebMapper {

  @Mapping(
      target = "currency",
      expression = "java(mapCurrency(request.getCurrency()))"
  )
  CreateCreditApplicationCommand toCommand(CreateCreditApplicationRequest request);


  @Mapping(
      target = "currency",
      expression = "java(mapCurrency(credit.getCurrency()))"
  )
  CreditApplicationResponse toResponse(CreditApplication credit);

  SearchCreditApplicationsQuery toQuery(
      Integer page,
      Integer size,
      String customerRfc,
      CreditStatus status,
      Double minAmount,
      Double maxAmount
  );

  @Mapping(source = "creditApplicationId", target = "creditApplicationId")
  @Mapping(source = "request.status", target = "status")
  @Mapping(source = "request.reason", target = "reason")
  ChangeCreditApplicationStatusCommand toCommand(
      UUID creditApplicationId,
      UpdateCreditStatusRequest request
  );


  default PagedCreditApplicationResponse toPagedResponse(PageResult<CreditApplication> pageResult) {
    var content = pageResult.content().stream()
        .map(this::toResponse)
        .toList();

    var metadata = new PageMetadata()
        .page(pageResult.metadata().page())
        .size(pageResult.metadata().size())
        .totalElements(pageResult.metadata().totalElements())
        .totalPages(pageResult.metadata().totalPages())
        .isLast(pageResult.metadata().isLast());

    return new PagedCreditApplicationResponse()
        .content(content)
        .metadata(metadata);
  }

  default Currency mapCurrency(Currencies currency){
    return switch (currency) {
      case MXN -> Currency.MXN;
    };
  }

  default Currencies mapCurrency(Currency currency){
    return switch (currency) {
      case MXN -> Currencies.MXN;
      default -> throw new IllegalArgumentException(
          "Unsuported API currency: " + currency
      );
    };
  }

  default BigDecimal map(Double value) {
    return value == null ? null : BigDecimal.valueOf(value);
  }

}
