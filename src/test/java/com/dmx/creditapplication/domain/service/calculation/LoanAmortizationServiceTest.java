package com.dmx.creditapplication.domain.service.calculation;

import com.dmx.creditapplication.domain.model.valueobject.MonthlyPaymentResult;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LoanAmortizationServiceTest {

  private final LoanAmortizationService service = new LoanAmortizationService();

  @Test
  void should_calculate_monthly_payment_when_interest_rate_is_zero() {
    // Arrange
    BigDecimal principal = new BigDecimal("12000");
    int months = 12;
    BigDecimal annualRate = BigDecimal.ZERO;

    // Act
    MonthlyPaymentResult result = service.calculate(principal, annualRate, months);

    // Assert
    assertEquals(
        0,
        result.monthlyPayment().compareTo(new BigDecimal("1000.00"))
    );
    assertEquals(
        0,
        result.totalToPay().compareTo(new BigDecimal("12000.00"))
    );
  }

  @Test
  void should_calculate_monthly_payment_with_interest_rate() {
    // Arrange
    var principal = BigDecimal.valueOf(10_000);
    var annualRate = BigDecimal.valueOf(0.10);
    int months = 12;

    // Act
    var result = service.calculate(principal, annualRate, months);

    // Assert
    assertEquals(
        0,
        result.monthlyPayment().compareTo(new BigDecimal("879.16"))
    );

    assertEquals(
        0,
        result.totalToPay().compareTo(new BigDecimal("10549.92"))
    );
  }

  @Test
  void  shoult_calculate_monthly_payment_for_long_term_credit() {
    // Arrange
    var principal = BigDecimal.valueOf(50_000);
    var annualRate = BigDecimal.valueOf(0.15);
    int months = 36;

    // Act
    var result = service.calculate(principal, annualRate, months);

    // Assert
    assertTrue(result.monthlyPayment().compareTo(BigDecimal.ZERO) > 0);
    assertTrue(result.totalToPay().compareTo(principal) > 0);

  }

  @Test
  void should_calculate_payment_for_small_amount() {
    // Arrange
    var principal = BigDecimal.valueOf(100);
    var annualRate = BigDecimal.valueOf(0.12);
    int months = 10;

    // Act
    var result = service.calculate(principal, annualRate, months);

    // Assert
    assertTrue(result.monthlyPayment().compareTo(BigDecimal.ZERO) > 0);
    assertTrue(result.totalToPay().compareTo(principal) > 0);
  }

}
