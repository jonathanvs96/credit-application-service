package com.dmx.creditapplication.domain.port.out;

import com.dmx.creditapplication.domain.model.entity.CreditApplication;
import com.dmx.creditapplication.domain.model.pagination.PageResult;
import com.dmx.creditapplication.domain.port.in.command.SearchCreditApplicationsQuery;

import java.util.Optional;
import java.util.UUID;

public interface CreditApplicationRepository {

  CreditApplication save(CreditApplication creditApplication);

  Optional<CreditApplication> findById(UUID creditApplicationId);

  PageResult<CreditApplication> search(SearchCreditApplicationsQuery query);

}
