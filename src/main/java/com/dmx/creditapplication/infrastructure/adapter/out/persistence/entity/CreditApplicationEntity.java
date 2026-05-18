package com.dmx.creditapplication.infrastructure.adapter.out.persistence.entity;

import com.dmx.creditapplication.domain.model.enums.CreditStatus;
import com.dmx.creditapplication.domain.model.enums.Currency;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;

import java.math.BigDecimal;
import java.sql.Types;
import java.time.LocalDate;

@Entity
@Table(name = "credit_applications")
@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreditApplicationEntity extends BaseEntity {

  @NotBlank
  @Size(max = 120)
  @Column(nullable = false, length = 120)
  private String customerName;

  @Email
  @NotBlank
  @Size(max = 160)
  @Column(nullable = false, length = 160)
  private String customerEmail;

  @Size(max = 13)
  @Column(length = 13)
  private String customerRfc;

  @NotNull
  @Digits(integer = 13, fraction = 2)
  @DecimalMin(value = "1.00")
  @Column(nullable = false, precision = 15, scale = 2)
  private BigDecimal requestedAmount;

  @NotNull
  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(Types.CHAR)
  @Column(nullable = false, length = 3)
  private Currency currency = Currency.MXN;

  @NotNull
  @Min(value = 6)
  @Max(value = 60)
  private Integer termMonths;

  @NotNull
  @Digits(integer = 1, fraction = 4)
  @Column(nullable = false, precision = 5, scale = 4)
  private BigDecimal annualInterestRate;

  @NotNull
  @Positive
  @Digits(integer = 13, fraction = 2)
  @Column(nullable = false, precision = 15, scale = 2)
  private BigDecimal monthlyPayment;

  @NotNull
  @Positive
  @Digits(integer = 13, fraction = 2)
  @Column(nullable = false, precision = 15, scale = 2)
  private BigDecimal totalToPay;

  @PositiveOrZero
  @Digits(integer = 13, fraction = 2)
  @Column(precision = 15, scale = 2)
  private BigDecimal amountUsd;

  @PositiveOrZero
  @Digits(integer = 13, fraction = 2)
  @Column(precision = 15, scale = 2)
  private BigDecimal amountEur;

  private LocalDate exchangeRateDate;

  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private CreditStatus status = CreditStatus.CREATED;

  private String statusReason;
}
