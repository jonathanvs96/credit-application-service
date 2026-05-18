package com.dmx.creditapplication.infrastructure.adapter.out.persistence.adapter;

import com.dmx.creditapplication.domain.model.entity.CreditApplication;
import com.dmx.creditapplication.domain.model.entity.ExchangeRates;
import com.dmx.creditapplication.domain.model.enums.CreditStatus;
import com.dmx.creditapplication.domain.model.enums.Currency;
import com.dmx.creditapplication.domain.model.pagination.PageResult;
import com.dmx.creditapplication.domain.port.in.command.SearchCreditApplicationsQuery;
import com.dmx.creditapplication.domain.port.out.CreditApplicationRepository;
import com.dmx.creditapplication.domain.service.calculation.LoanAmortizationService;
import com.dmx.creditapplication.infrastructure.adapter.out.persistence.PostgreSQLContainerConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@Transactional
public class CreditApplicationRepositoryAdapterIntegrationTest extends PostgreSQLContainerConfig {

  @Autowired
  private CreditApplicationRepository repository;

  @Nested
  @DisplayName("save")
  class SaveTests {

    @Test
    void should_persist_credit_application() {
      // Arrange
      CreditApplication creditApplication = CreditApplication.create(
          "Juan Perez",
          "JUAP900101ABC",
          "GODE561231GR8",
          new BigDecimal("100000"),
          Currency.MXN,
          12,
          new BigDecimal("0.2500")
      );

      // Act
      CreditApplication saved = repository.save(creditApplication);

      // Assert
      assertThat(saved.getId()).isNotNull();
      assertThat(saved.getCustomerName()).isEqualTo("Juan Perez");
      assertThat(saved.getStatus()).isNotNull();
    }

  }

  @Nested
  @DisplayName("findById")
  class FindByIdTests {

    @Test
    void should_find_credit_application_by_id() {
      // Arrange
      CreditApplication saved = createAndSave(
          "GODE561231GR8",
          CreditStatus.CREATED,
          BigDecimal.valueOf(10_000),
          Currency.MXN
      );

      // Act
      Optional<CreditApplication> result = repository.findById(saved.getId());

      // Assert
      assertThat(result).isPresent();
      assertThat(result.get().getId()).isEqualTo(saved.getId());
    }

    @Test
    void should_not_find_credit_application() {
      // Arrange
      CreditApplication creditApplication = CreditApplication.create(
          "Juan Perez",
          "juan@example.com",
          "GODE561231GR8",
          new BigDecimal("100000"),
          Currency.MXN,
          12,
          new BigDecimal("0.2500")
      );

      // Act
      Optional<CreditApplication> result = repository.findById(creditApplication.getId());

      // Assert
      assertThat(result).isEmpty();
    }
  }

  @Nested
  class Search {

    @Test
    void should_filter_by_customer_rfc() {

      createAndSave("RFC1", CreditStatus.CREATED, new BigDecimal("100000"), Currency.MXN);
      createAndSave("RFC2", CreditStatus.CREATED, new BigDecimal("100000"), Currency.MXN);

      var query = new SearchCreditApplicationsQuery(
          0, 10,
          "RFC1",
          null,
          null,
          null
      );

      PageResult<CreditApplication> result = repository.search(query);

      assertThat(result.content()).hasSize(1);
      assertThat(result.content().get(0).getCustomerRfc()).isEqualTo("RFC1");
    }

    @Test
    void should_filter_by_status() {

      CreditApplication rfc2 = createAndSave("RFC2", CreditStatus.CREATED, new BigDecimal("10000"), Currency.MXN);
      createAndSave("RFC1", CreditStatus.CREATED, new BigDecimal("10000"), Currency.MXN);

      rfc2.changeStatus(CreditStatus.UNDER_REVIEW, null);
      repository.save(rfc2);

      var query = new SearchCreditApplicationsQuery(
          0, 10,
          null,
          CreditStatus.UNDER_REVIEW,
          null,
          null
      );

      PageResult<CreditApplication> result = repository.search(query);

      assertThat(result.content())
          .hasSize(1)
          .allMatch(app -> app.getStatus() == CreditStatus.UNDER_REVIEW);
    }

    @Test
    void should_filter_by_amount_range() {

      createAndSave("RFC1", CreditStatus.CREATED, new BigDecimal("5000"), Currency.MXN);
      createAndSave("RFC2", CreditStatus.CREATED, new BigDecimal("15000"), Currency.MXN);

      var query = new SearchCreditApplicationsQuery(
          0, 10,
          null,
          null,
          new BigDecimal("10000"),
          new BigDecimal("20000")
      );

      PageResult<CreditApplication> result = repository.search(query);

      assertThat(result.content()).hasSize(1);
      assertThat(result.content().get(0).getRequestedAmount())
          .isGreaterThanOrEqualTo(new BigDecimal("10000"));
    }

    @Test
    void should_return_paginated_results() {

      for (int i = 0; i < 15; i++) {
        createAndSave("RFC" + i, CreditStatus.CREATED, new BigDecimal("10000"), Currency.MXN);
      }

      var query = new SearchCreditApplicationsQuery(
          0, 5,
          null,
          null,
          null,
          null
      );

      PageResult<CreditApplication> result = repository.search(query);

      assertThat(result.content()).hasSize(5);
      assertThat(result.metadata().totalElements()).isEqualTo(15);
      assertThat(result.metadata().totalPages()).isEqualTo(3);
    }

    @Test
    void should_return_empty_result_when_no_match() {

      createAndSave("RFC1", CreditStatus.CREATED, new BigDecimal("10000"), Currency.MXN);

      var query = new SearchCreditApplicationsQuery(
          0, 10,
          "NOT_EXIST",
          null,
          null,
          null
      );

      PageResult<CreditApplication> result = repository.search(query);

      assertThat(result.content()).isEmpty();
    }
  }

  private CreditApplication createAndSave(
      String rfc,
      CreditStatus status,
      BigDecimal amount,
      Currency currency
  ) {
    CreditApplication app = CreditApplication.create(
        "Test User",
        "test@example.com",
        rfc,
        amount,
        currency,
        12,
        new BigDecimal("0.2500")
    );

    // si necesitas cambiar status para test:
    if (status != CreditStatus.CREATED) {
      app.changeStatus(status, null);
    }

    app.calculateExchangeAmounts(new ExchangeRates(
        Currency.MXN.name(),
        LocalDate.now(),
        Map.of(
            "USD", new BigDecimal("0.05"),
            "EUR", new BigDecimal("0.04")
        )
    ));
    app.calculatePayments(new LoanAmortizationService());
    return repository.save(app);
  }

}
