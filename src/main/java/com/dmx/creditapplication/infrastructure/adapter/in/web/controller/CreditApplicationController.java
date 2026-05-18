package com.dmx.creditapplication.infrastructure.adapter.in.web.controller;

import com.dmx.creditapplication.domain.port.in.usecases.ChangeCreditApplicationStatusUseCase;
import com.dmx.creditapplication.domain.port.in.usecases.CreateCreditApplicationUseCase;
import com.dmx.creditapplication.domain.port.in.usecases.GetCreditApplicationUseCase;
import com.dmx.creditapplication.domain.port.in.usecases.SearchCreditApplicationsUseCase;
import com.dmx.creditapplication.infrastructure.adapter.in.web.api.CreditApplicationsApi;
import com.dmx.creditapplication.infrastructure.adapter.in.web.dto.*;
import com.dmx.creditapplication.infrastructure.adapter.in.web.mapper.CreditApplicationWebMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class CreditApplicationController implements CreditApplicationsApi {

  private final CreateCreditApplicationUseCase createCreditUseCase;
  private final GetCreditApplicationUseCase getCreditApplicationUseCase;
  private final SearchCreditApplicationsUseCase searchCreditApplicationsUseCase;
  private final ChangeCreditApplicationStatusUseCase changeCreditApplicationStatusUseCase;
  private final CreditApplicationWebMapper mapper;

  @Override
  public ResponseEntity<CreditApplicationResponse> changeCreditApplicationStatus(UUID creditApplicationId, UpdateCreditStatusRequest request) {
    var command = mapper.toCommand(creditApplicationId, request);
    var creditApplication = changeCreditApplicationStatusUseCase.execute(command);

    return ResponseEntity.ok(mapper.toResponse(creditApplication));
  }

  @Override
  public ResponseEntity<CreditApplicationResponse> createCreditApplication(CreateCreditApplicationRequest request) {
    var command = mapper.toCommand(request);
    var createdCredit = createCreditUseCase.create(command);
    var response = mapper.toResponse(createdCredit);

    URI location = ServletUriComponentsBuilder
        .fromCurrentRequest()
        .path("/{id}")
        .buildAndExpand(createdCredit.getId())
        .toUri();
    return ResponseEntity.created(location).body(response);
  }

  @Override
  public ResponseEntity<CreditApplicationResponse> getCreditApplication(UUID creditApplicationId) {
    var creditApplication = getCreditApplicationUseCase.get(creditApplicationId);
    return ResponseEntity.ok(mapper.toResponse(creditApplication));
  }

  @Override
  public ResponseEntity<PagedCreditApplicationResponse> getCreditApplications(Integer page, Integer size, String customerRfc, CreditStatus status, Double minAmount, Double maxAmount) {
    var query = mapper.toQuery(page, size, customerRfc, status, minAmount, maxAmount);
    var pageResult = searchCreditApplicationsUseCase.execute(query);
    var pageResponse = mapper.toPagedResponse(pageResult);

    return ResponseEntity.ok(pageResponse);
  }

}
