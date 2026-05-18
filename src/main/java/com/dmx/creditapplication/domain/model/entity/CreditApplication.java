package com.dmx.creditapplication.domain.model.entity;

import com.dmx.creditapplication.domain.exception.ConflictException;
import com.dmx.creditapplication.domain.model.valueobject.MonthlyPaymentResult;
import com.dmx.creditapplication.domain.model.enums.CreditStatus;
import com.dmx.creditapplication.domain.model.enums.Currency;
import com.dmx.creditapplication.domain.model.state.CreditStatusTransitions;
import com.dmx.creditapplication.domain.service.calculation.LoanAmortizationService;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
public class CreditApplication {

  private final UUID id;

  private String customerName;
  private String customerEmail;
  private String customerRfc;

  private BigDecimal requestedAmount;
  private Currency currency;
  private Integer termMonths;
  private BigDecimal annualInterestRate;

  private BigDecimal monthlyPayment;
  private BigDecimal totalToPay;

  private BigDecimal amountUsd;
  private BigDecimal amountEur;
  private LocalDate exchangeRateDate;

  private CreditStatus status;
  private String statusReason;

  private OffsetDateTime createdAt;
  private OffsetDateTime updatedAt;

  private CreditApplication(
      UUID id,
      String customerName,
      String customerEmail,
      String customerRfc,
      BigDecimal requestedAmount,
      Currency currency,
      Integer termMonths,
      BigDecimal annualInterestRate
      ){
    this.id = id;
    this.customerName = customerName;
    this.customerEmail = customerEmail;
    this.customerRfc = customerRfc;
    this.requestedAmount = requestedAmount;
    this.currency = currency;
    this.termMonths = termMonths;
    this.annualInterestRate = annualInterestRate;
    this.status = CreditStatus.CREATED;
  }

  private CreditApplication(
      UUID id,
      String customerName,
      String customerEmail,
      String customerRfc,
      BigDecimal requestedAmount,
      Currency currency,
      Integer termMonths,
      BigDecimal annualInterestRate,
      BigDecimal monthlyPayment,
      BigDecimal totalToPay,
      BigDecimal amountUsd,
      BigDecimal amountEur,
      LocalDate exchangeRateDate,
      CreditStatus status,
      String statusReason,
      OffsetDateTime createdAt,
      OffsetDateTime updatedAt
  ){
    this.id = id;
    this.customerName = customerName;
    this.customerEmail = customerEmail;
    this.customerRfc = customerRfc;
    this.requestedAmount = requestedAmount;
    this.currency = currency;
    this.termMonths = termMonths;
    this.annualInterestRate = annualInterestRate;
    this.monthlyPayment = monthlyPayment;
    this.totalToPay = totalToPay;
    this.amountUsd = amountUsd;
    this.amountEur = amountEur;
    this.exchangeRateDate = exchangeRateDate;
    this.status = status;
    this.statusReason = statusReason;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }


  public static CreditApplication create(
      String customerName,
      String customerEmail,
      String customerRfc,
      BigDecimal requestedAmount,
      Currency currency,
      Integer termMonths,
      BigDecimal annualInterestRate
  ) {
    return new CreditApplication(
        UUID.randomUUID(),
        customerName,
        customerEmail,
        customerRfc,
        requestedAmount,
        currency,
        termMonths,
        annualInterestRate
    );
  }

  public static CreditApplication restore(
      UUID id,
      String customerName,
      String customerEmail,
      String customerRfc,
      BigDecimal requestedAmount,
      Currency currency,
      Integer termMonths,
      BigDecimal annualInterestRate,
      BigDecimal monthlyPayment,
      BigDecimal totalToPay,
      BigDecimal amountUsd,
      BigDecimal amountEur,
      LocalDate exchangeRateDate,
      CreditStatus status,
      String statusReason,
      OffsetDateTime createdAt,
      OffsetDateTime updatedAt) {
    return new CreditApplication(
        id,
        customerName,
        customerEmail,
        customerRfc,
        requestedAmount,
        currency,
        termMonths,
        annualInterestRate,
        monthlyPayment,
        totalToPay,
        amountUsd,
        amountEur,
        exchangeRateDate,
        status,
        statusReason,
        createdAt,
        updatedAt
      );
  }

  public void calculatePayments(LoanAmortizationService service) {
    MonthlyPaymentResult result =
        service.calculate(
            this.requestedAmount,
            this.annualInterestRate,
            this.termMonths
        );

    this.monthlyPayment = result.monthlyPayment();
    this.totalToPay = result.totalToPay();
  }

  public void calculateExchangeAmounts(ExchangeRates exchangeRates) {
    var rates = exchangeRates.rates();

    var rateUsd = rates.get(Currency.USD.name());
    if(rateUsd != null){
      this.amountUsd = this.requestedAmount.multiply(rateUsd)
          .setScale(2, RoundingMode.HALF_UP);
    }

    var rateEur = rates.get(Currency.EUR.name());
    if(rateEur != null) {
      this.amountEur = this.requestedAmount.multiply(rateEur)
          .setScale(2, RoundingMode.HALF_UP);
    }

    this.exchangeRateDate = exchangeRates.date();
  }

  public void changeStatus(CreditStatus newStatus, String statusReason) {
    if (!CreditStatusTransitions.canTransition(this.status, newStatus)) {
      throw new ConflictException("Invalid status transition from " + status + " to " + newStatus);
    }
    this.status = newStatus;
    this.statusReason = statusReason;
  }

}
