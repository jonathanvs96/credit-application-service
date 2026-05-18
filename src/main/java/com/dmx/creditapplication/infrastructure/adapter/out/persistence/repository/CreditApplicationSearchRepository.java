package com.dmx.creditapplication.infrastructure.adapter.out.persistence.repository;

import com.dmx.creditapplication.infrastructure.adapter.out.persistence.entity.CreditApplicationEntity;
import com.dmx.creditapplication.infrastructure.adapter.out.persistence.querydsl.creditapplication.CreditApplicationSearchModel;
import org.springframework.data.domain.Page;

public interface CreditApplicationSearchRepository {

  Page<CreditApplicationEntity> search(CreditApplicationSearchModel query);

}
