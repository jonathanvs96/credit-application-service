package com.dmx.creditapplication.infrastructure.adapter.out.persistence.querydsl.creditapplication;

import com.dmx.creditapplication.infrastructure.adapter.out.persistence.entity.CreditApplicationEntity;
import com.dmx.creditapplication.infrastructure.adapter.out.persistence.entity.QCreditApplicationEntity;
import com.dmx.creditapplication.infrastructure.adapter.out.persistence.repository.CreditApplicationSearchRepository;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class CreditApplicationQueryDslRepositoryImpl
    implements CreditApplicationSearchRepository {

  private final JPAQueryFactory queryFactory;
  private final CreditApplicationPredicateBuilder predicateBuilder;

  public CreditApplicationQueryDslRepositoryImpl(
      JPAQueryFactory queryFactory,
      CreditApplicationPredicateBuilder predicateBuilder
  ) {
    this.queryFactory = queryFactory;
    this.predicateBuilder = predicateBuilder;
  }

  @Override
  public Page<CreditApplicationEntity> search(CreditApplicationSearchModel query) {
    QCreditApplicationEntity credit = QCreditApplicationEntity.creditApplicationEntity;

    BooleanBuilder predicate = predicateBuilder.build(query);

    Pageable pageable = PageRequest.of(query.page(),query.size());

    List<CreditApplicationEntity> content = queryFactory
        .selectFrom(credit)
        .where(predicate)
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    long total = Optional.ofNullable(
            queryFactory
                .select(credit.count())
                .from(credit)
                .where(predicate)
                .fetchOne()
        )
        .orElse(0L);

    return new PageImpl<>(content, pageable, total);
  }
}
