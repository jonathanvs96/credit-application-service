package com.dmx.creditapplication.application.service;

import com.dmx.creditapplication.domain.exception.ConflictException;
import com.dmx.creditapplication.domain.exception.ResourceNotFoundException;
import com.dmx.creditapplication.domain.model.entity.CreditApplication;
import com.dmx.creditapplication.domain.model.entity.ExchangeRates;
import com.dmx.creditapplication.domain.model.enums.CreditStatus;
import com.dmx.creditapplication.domain.model.enums.Currency;
import com.dmx.creditapplication.domain.model.pagination.PageMetadata;
import com.dmx.creditapplication.domain.model.pagination.PageResult;
import com.dmx.creditapplication.domain.port.in.command.ChangeCreditApplicationStatusCommand;
import com.dmx.creditapplication.domain.port.in.command.CreateCreditApplicationCommand;
import com.dmx.creditapplication.domain.port.in.command.SearchCreditApplicationsQuery;
import com.dmx.creditapplication.domain.port.out.CreditApplicationRepository;
import com.dmx.creditapplication.domain.port.out.ExchangeRateProvider;
import com.dmx.creditapplication.domain.service.calculation.LoanAmortizationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CreditApplicationServiceTest {

  @Mock
  private CreditApplicationRepository repository;

  @Mock
  private ExchangeRateProvider exchangeRateProvider;

  private CreditApplicationService service;

  @BeforeEach
  void setUp() {
    LoanAmortizationService amortizationService = new LoanAmortizationService();
    service = new CreditApplicationService(
        repository,
        amortizationService,
        exchangeRateProvider
    );
  }

  @Nested
  class Create {

    @Nested
    class TestingExchangeRates {

      @Test
      void should_create_credit_application_with_exchange_amounts() {
        // Arrange
        var command = baseCommand();
        var exchangeRates = baseExchangeRates();

        when(exchangeRateProvider.getRate(
            anyString(),
            anyString(),
            anyString()))
            .thenReturn(exchangeRates);

        when(repository.save(any(CreditApplication.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        var result = service.create(command);

        // Assert
        assertAll(
            "Exchange amounts",
            () -> assertNotNull(result.getAmountUsd()),
            () -> assertNotNull(result.getAmountEur()),
            () -> assertEquals(
                0,
                result.getAmountUsd().compareTo(new BigDecimal("50.00"))
            ),
            () -> assertEquals(
                0,
                result.getAmountEur().compareTo(new BigDecimal("40.00"))
            )
        );

        baseVerify();
      }

      @Test
      void should_create_credit_application_with_null_exchange_amounts_when_provider_fails() {
        // Arrange
        var command = baseCommand();
        var fallbackExchangeRates = new ExchangeRates(
            Currency.MXN.name(),
            null,
            Collections.emptyMap()
        );

        when(exchangeRateProvider.getRate(
            anyString(),
            anyString(),
            anyString()))
            .thenReturn(fallbackExchangeRates);
        when(repository.save(any(CreditApplication.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        CreditApplication result = service.create(command);

        // Assert
        assertAll(
            "Exchange amounts and date",
            () -> assertNull(result.getAmountUsd()),
            () -> assertNull(result.getAmountEur()),
            () -> assertNull(result.getExchangeRateDate())
        );
        baseVerify();
      }

    }

    @Test
    void should_calculate_monthly_payment_and_total_to_pay() {
      // Arrange
      var command = baseCommand();
      var exchangeRates = baseExchangeRates();

      when(exchangeRateProvider.getRate(
          anyString(),
          anyString(),
          anyString()))
          .thenReturn(exchangeRates);

      when(repository.save(any(CreditApplication.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      // Act
      var result = service.create(command);

      // Assert
      assertAll(
          "Checking amounts",
          () -> assertTrue(
              result.getTotalToPay()
                  .compareTo(result.getRequestedAmount()) >= 0
          ),
          () -> assertTrue(
              result.getTotalToPay()
                  .compareTo(result.getMonthlyPayment()) >= 0
          )
      );
      baseVerify();
    }

    @Test
    void should_create_credit_application_with_created_status(){

      // Arrange
      var command = baseCommand();
      var exchangeRates = baseExchangeRates();

      when(exchangeRateProvider.getRate(
          anyString(),
          anyString(),
          anyString()))
          .thenReturn(exchangeRates);

      when(repository.save(any(CreditApplication.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      // Act
      var result = service.create(command);

      // Assert
      assertEquals(
          CreditStatus.CREATED,
          result.getStatus()
      );
      baseVerify();
    }

    @Test
    void should_persist_credit_application_with_calculated_values(){

      // Arrange
      var command = baseCommand().toBuilder()
          .annualInterestRate(BigDecimal.valueOf(0.12))
          .build();

      var exchangeRates = baseExchangeRates();

      when(exchangeRateProvider.getRate(
          anyString(),
          anyString(),
          anyString()))
          .thenReturn(exchangeRates);

      when(repository.save(any(CreditApplication.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      ArgumentCaptor<CreditApplication> captor = ArgumentCaptor.forClass(CreditApplication.class);

      // Act
      service.create(command);

      // Assert
      verify(exchangeRateProvider, times(1)).getRate(
          eq("MXN"),
          eq("USD"),
          eq("EUR")
      );
      verify(repository, times(1)).save(captor.capture());


      CreditApplication savedCredit = captor.getValue();

      assertAll(
          "Credit Values",
          () -> assertNotNull(savedCredit.getMonthlyPayment()),
          () -> assertNotNull(savedCredit.getTotalToPay()),
          () -> assertNotNull(savedCredit.getAmountUsd()),
          () -> assertNotNull(savedCredit.getAmountEur()),
          () -> assertEquals(
              CreditStatus.CREATED,
              savedCredit.getStatus()
          ),
          () -> assertEquals(
              0,
              savedCredit.getAmountUsd()
                  .compareTo(new BigDecimal("50.00"))
          ),
          () -> assertEquals(
              0,
              savedCredit.getAmountEur()
                  .compareTo(new BigDecimal("40.00"))
          )
      );
    }


    private void baseVerify() {
      verify(exchangeRateProvider, times(1)).getRate(
          eq("MXN"),
          eq("USD"),
          eq("EUR")
      );
      verify(repository, times(1)).save(any(CreditApplication.class));
    }

  }

  @Nested
  class Get {

    @Test
    void should_return_credit_application_when_it_exists() {
      // Arrange
      var credit = testCredit();

      when(repository.findById(credit.getId()))
          .thenReturn(Optional.of(credit));

      // Act
      CreditApplication result = service.get(credit.getId());

      // Assert
      assertAll(
          () -> assertNotNull(result),
          () -> assertEquals(
              credit.getId(),
              result.getId()
          ),
          () -> assertEquals(
              credit.getStatus(),
              result.getStatus()
          )
      );

      verify(repository, times(1))
          .findById(credit.getId());
    }

    @Test
    void should_throw_exception_when_credit_application_does_not_exist() {

      // Arrange
      UUID creditId = UUID.randomUUID();

      when(repository.findById(creditId))
          .thenReturn(Optional.empty());

      // Act & Assert
      assertThrows(
          ResourceNotFoundException.class,
          () -> service.get(creditId)
      );

      verify(repository, times(1))
          .findById(creditId);
    }

  }

  @Test
  void should_delegate_search_to_repository() {
    // Arrange
    SearchCreditApplicationsQuery query =
        new SearchCreditApplicationsQuery(
            0,
            10,
            "XAXX010101000",
            CreditStatus.CREATED,
            new BigDecimal("1000"),
            new BigDecimal("5000")
        );

    PageResult<CreditApplication> expectedResult =
        new PageResult<>(
            List.of(),
            new PageMetadata(
                0,
                10,
                0,
                0,
                true
            )
        );

    when(repository.search(query))
        .thenReturn(expectedResult);

    // Act
    PageResult<CreditApplication> result =
        service.execute(query);

    // Assert
    assertSame(expectedResult, result);

    verify(repository, times(1))
        .search(query);
  }

  @Nested
  class ChangeStatus {

    @Test
    void should_change_status_when_transition_is_valid() {
      // Arrange
      CreditApplication credit = testCredit();
      ChangeCreditApplicationStatusCommand command = statusCommandUnderReview(credit.getId());

      when(repository.findById(credit.getId()))
          .thenReturn(Optional.of(credit));
      when(repository.save(any(CreditApplication.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      // Act
      CreditApplication result =
          service.execute(command);
      // Assert
      assertAll(
          () -> assertEquals(
              CreditStatus.UNDER_REVIEW,
              result.getStatus()
          ),

          () -> assertEquals(
              "Review started",
              result.getStatusReason()
          )
      );
      verify(repository, times(1))
          .findById(credit.getId());
      verify(repository, times(1))
          .save(credit);
    }

    @Test
    void should_throw_exception_when_credit_application_does_not_exist() {
      // Arrange
      UUID creditId = UUID.randomUUID();
      ChangeCreditApplicationStatusCommand command = statusCommandUnderReview(creditId);

      when(repository.findById(creditId))
          .thenReturn(Optional.empty());

      // Act & Assert
      assertThrows(
          ResourceNotFoundException.class,
          () -> service.execute(command)
      );

      verify(repository, times(1))
          .findById(creditId);

      verify(repository, never())
          .save(any());
    }

    @Test
    void should_throw_exception_when_transition_is_invalid() {

      // Arrange
      CreditApplication credit = testCredit();

      ChangeCreditApplicationStatusCommand command =
          new ChangeCreditApplicationStatusCommand(
              credit.getId(),
              CreditStatus.APPROVED,
              "Invalid transition"
          );

      when(repository.findById(credit.getId()))
          .thenReturn(Optional.of(credit));

      // Act & Assert
      assertThrows(
          ConflictException.class,
          () -> service.execute(command)
      );

      verify(repository, times(1))
          .findById(credit.getId());

      verify(repository, never())
          .save(any());
    }

    private ChangeCreditApplicationStatusCommand statusCommandUnderReview(UUID creditId) {
      return new ChangeCreditApplicationStatusCommand(
          creditId,
          CreditStatus.UNDER_REVIEW,
          "Review started"
      );
    }

  }

  private CreditApplication testCredit() {
    return
        CreditApplication.restore(
            UUID.randomUUID(),
            "Ivan",
            "ivan@test.com",
            "XAXX010101000",
            new BigDecimal("1000"),
            Currency.MXN,
            12,
            new BigDecimal("0.12"),
            new BigDecimal("88.85"),
            new BigDecimal("1066.20"),
            new BigDecimal("50.00"),
            new BigDecimal("40.00"),
            LocalDate.now(),
            CreditStatus.CREATED,
            null,
            OffsetDateTime.now(),
            OffsetDateTime.now()
        );
  }

  private CreateCreditApplicationCommand baseCommand() {
    return CreateCreditApplicationCommand.builder()
        .customerName("Ivan")
        .customerEmail("ivan@test.com")
        .customerRfc("XAXX010101000")
        .requestedAmount(BigDecimal.valueOf(1000))
        .currency(Currency.MXN)
        .termMonths(12)
        .annualInterestRate(BigDecimal.ZERO)
        .build();
  }

  private ExchangeRates baseExchangeRates() {
    return new ExchangeRates(
        Currency.MXN.name(),
        LocalDate.now(),
        Map.of(
            "USD", new BigDecimal("0.05"),
            "EUR", new BigDecimal("0.04")
        )
    );
  }

}
