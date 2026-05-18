package com.dmx.creditapplication.domain.port.in.usecases;

import com.dmx.creditapplication.domain.model.entity.CreditApplication;
import com.dmx.creditapplication.domain.model.pagination.PageResult;
import com.dmx.creditapplication.domain.port.in.command.SearchCreditApplicationsQuery;

public interface SearchCreditApplicationsUseCase {

  PageResult<CreditApplication> execute(SearchCreditApplicationsQuery query);

}
