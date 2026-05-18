package com.dmx.creditapplication.domain.service.calculation;

import com.dmx.creditapplication.domain.model.valueobject.MonthlyPaymentResult;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class LoanAmortizationService {

  private static final int SCALE_CALC = 10;
  private static final int SCALE_RESULT = 2;

  public MonthlyPaymentResult calculate(
      BigDecimal principal,
      BigDecimal annualInterestRate,
      int termMonths
  ) {
    BigDecimal monthlyRate = annualInterestRate
        .divide(BigDecimal.valueOf(12), SCALE_CALC, RoundingMode.HALF_UP);

    if(monthlyRate.compareTo(BigDecimal.ZERO) == 0){
      BigDecimal monthlyPayment = principal.divide(
          BigDecimal.valueOf(termMonths),
          SCALE_RESULT,
          RoundingMode.HALF_UP
      );
      return MonthlyPaymentResult.builder()
          .monthlyPayment(monthlyPayment)
          .totalToPay(principal.setScale(SCALE_RESULT, RoundingMode.HALF_UP))
          .build();
    }

    BigDecimal onePlusRate = BigDecimal.ONE.add(monthlyRate);
    BigDecimal power = onePlusRate.pow(termMonths);
    BigDecimal denominator = BigDecimal.ONE.subtract(
        BigDecimal.ONE.divide(power, SCALE_CALC, RoundingMode.HALF_UP)
    );
    BigDecimal montlyPayment = principal
        .multiply(monthlyRate)
        .divide(denominator, SCALE_RESULT, RoundingMode.HALF_UP);
    BigDecimal totalToPay = montlyPayment
        .multiply(BigDecimal.valueOf(termMonths))
        .setScale(SCALE_RESULT, RoundingMode.HALF_UP);

    return MonthlyPaymentResult.builder()
        .monthlyPayment(montlyPayment)
        .totalToPay(totalToPay)
        .build();
  }

}
