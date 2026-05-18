package com.dmx.creditapplication.domain.model.entity;

import com.dmx.creditapplication.domain.exception.ConflictException;
import com.dmx.creditapplication.domain.model.enums.CreditStatus;
import com.dmx.creditapplication.domain.model.enums.Currency;
import com.dmx.creditapplication.domain.service.calculation.LoanAmortizationService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class CreditApplicationTest {

  @Test
  void should_create_credit_application_with_created_status() {
    // Arrange
    var amount = BigDecimal.valueOf(10_000);

    // Act
    var application = CreditApplication.create(
        "Ivan",
        "ivan@test.com",
        "XAXX010101000",
        amount,
        Currency.MXN,
        12,
        new BigDecimal("0.10")
    );

    // Assert
    assertNotNull(application.getId());

    assertEquals(
        0,
        application.getRequestedAmount().compareTo(amount)
    );
  }

  @Test
  void should_calculate_monthly_payment_and_total_to_pay() {
    // Arrange
    var service = new LoanAmortizationService();

    var application = CreditApplication.create(
        "Ivan",
        "ivan@test.com",
        "XAXX010101000",
        new BigDecimal("12000"),
        Currency.MXN,
        12,
        BigDecimal.ZERO
    );

    // Act
    application.calculatePayments(service);

    // Assert
    assertEquals(
        0,
        application.getMonthlyPayment()
            .compareTo(new BigDecimal("1000.00"))
    );

    assertEquals(
        0,
        application.getTotalToPay()
            .compareTo(new BigDecimal("12000.00"))
    );
  }

  @Test
  void should_calculate_exchange_amounts() {
    // Arrange
    Map<String, BigDecimal> rates = Map.of(
        "USD", new BigDecimal("0.05"),
        "EUR", new BigDecimal("0.04")
    );

    var exchangeRates =
        new ExchangeRates(
            Currency.MXN.name(),
            LocalDate.now(),
            rates
        );

    CreditApplication application = CreditApplication.create(
        "Ivan",
        "ivan@test.com",
        "XAXX010101000",
        new BigDecimal("1000"),
        Currency.MXN,
        12,
        new BigDecimal("0.10")
    );

    // Act
    application.calculateExchangeAmounts(exchangeRates);

    // Assert
    assertEquals(
        0,
        application.getAmountUsd()
            .compareTo(new BigDecimal("50.00"))
    );
    assertEquals(
        0,
        application.getAmountEur()
            .compareTo(new BigDecimal("40.00"))
    );
    assertNotNull(application.getExchangeRateDate());
  }

  @Test
  void should_not_calculate_eur_amount_when_rate_is_missing() {
    // Arrange
    Map<String, BigDecimal> rates = Map.of(
        "USD", new BigDecimal("0.05")
    );

    var exchangeRates = new ExchangeRates(
        Currency.MXN.name(),
        LocalDate.now(),
        rates
    );


    CreditApplication application =
        CreditApplication.create(
            "Ivan",
            "ivan@test.com",
            "XAXX010101000",
            new BigDecimal("1000"),
            Currency.MXN,
            12,
            new BigDecimal("0.10")
        );

    // Act
    application.calculateExchangeAmounts(exchangeRates);

    // Assert
    assertNotNull(application.getAmountUsd());

    assertNull(application.getAmountEur());
  }

  @Test
  void should_change_status_when_transition_is_valid() {

    // Arrange
    CreditApplication application =
        CreditApplication.create(
            "Ivan",
            "ivan@test.com",
            "XAXX010101000",
            new BigDecimal("1000"),
            Currency.MXN,
            12,
            new BigDecimal("0.10")
        );

    // Act
    application.changeStatus(
        CreditStatus.UNDER_REVIEW,
        "Review started"
    );

    // Assert
    assertEquals(
        CreditStatus.UNDER_REVIEW,
        application.getStatus()
    );

    assertEquals(
        "Review started",
        application.getStatusReason()
    );
  }

  @Test
  void should_throw_exception_when_transition_is_invalid() {
    // Arrange
    CreditApplication application =
        CreditApplication.create(
            "Ivan",
            "ivan@test.com",
            "XAXX010101000",
            new BigDecimal("1000"),
            Currency.MXN,
            12,
            new BigDecimal("0.10")
        );

    // Act & Assert
    assertThrows(
        ConflictException.class,
        () -> application.changeStatus(
            CreditStatus.APPROVED,
            "Invalid transition"
        )
    );
  }

}
