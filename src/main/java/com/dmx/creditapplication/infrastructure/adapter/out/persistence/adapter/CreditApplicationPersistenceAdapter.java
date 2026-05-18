package com.dmx.creditapplication.infrastructure.adapter.out.persistence.adapter;

import com.dmx.creditapplication.domain.model.entity.CreditApplication;
import com.dmx.creditapplication.domain.model.pagination.PageResult;
import com.dmx.creditapplication.domain.port.in.command.SearchCreditApplicationsQuery;
import com.dmx.creditapplication.domain.port.out.CreditApplicationRepository;
import com.dmx.creditapplication.infrastructure.adapter.out.persistence.entity.CreditApplicationEntity;
import com.dmx.creditapplication.infrastructure.adapter.out.persistence.mapper.CreditApplicationPersistenceMapper;
import com.dmx.creditapplication.infrastructure.adapter.out.persistence.querydsl.creditapplication.CreditApplicationSearchModel;
import com.dmx.creditapplication.infrastructure.adapter.out.persistence.repository.CreditApplicationSearchRepository;
import com.dmx.creditapplication.infrastructure.adapter.out.persistence.repository.CreditApplicationJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CreditApplicationPersistenceAdapter implements CreditApplicationRepository {

  private final CreditApplicationJpaRepository jpaRepository;
  private final CreditApplicationPersistenceMapper mapper;
  private final CreditApplicationSearchRepository queryService;

  @Override
  public CreditApplication save(CreditApplication creditApplication) {
    var creditApplicationEntity = mapper.toEntity(creditApplication);

    var savedEntity = jpaRepository.save(creditApplicationEntity);

    return mapper.toDomain(savedEntity);
  }

  @Override
  public Optional<CreditApplication> findById(UUID creditApplicationId) {
    return this.jpaRepository.findById(creditApplicationId)
        .map(mapper::toDomain);
  }

  @Override
  public PageResult<CreditApplication> search(SearchCreditApplicationsQuery query) {
    CreditApplicationSearchModel model = mapper.toSearchModel(query);

    Page<CreditApplicationEntity> result = queryService.search(model);
    return mapper.toPageResult(result);
  }

}
