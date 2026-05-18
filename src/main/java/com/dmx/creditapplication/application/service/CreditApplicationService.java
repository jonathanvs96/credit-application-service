package com.dmx.creditapplication.application.service;

import com.dmx.creditapplication.domain.exception.ResourceNotFoundException;
import com.dmx.creditapplication.domain.model.entity.CreditApplication;
import com.dmx.creditapplication.domain.model.entity.ExchangeRates;
import com.dmx.creditapplication.domain.model.pagination.PageResult;
import com.dmx.creditapplication.domain.port.in.usecases.ChangeCreditApplicationStatusUseCase;
import com.dmx.creditapplication.domain.port.in.usecases.CreateCreditApplicationUseCase;
import com.dmx.creditapplication.domain.port.in.usecases.GetCreditApplicationUseCase;
import com.dmx.creditapplication.domain.port.in.usecases.SearchCreditApplicationsUseCase;
import com.dmx.creditapplication.domain.port.in.command.ChangeCreditApplicationStatusCommand;
import com.dmx.creditapplication.domain.port.in.command.CreateCreditApplicationCommand;
import com.dmx.creditapplication.domain.port.in.command.SearchCreditApplicationsQuery;
import com.dmx.creditapplication.domain.port.out.CreditApplicationRepository;
import com.dmx.creditapplication.domain.port.out.ExchangeRateProvider;
import com.dmx.creditapplication.domain.service.calculation.LoanAmortizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreditApplicationService implements  CreateCreditApplicationUseCase,
                                                  GetCreditApplicationUseCase,
                                                  SearchCreditApplicationsUseCase,
                                                  ChangeCreditApplicationStatusUseCase {
  private final CreditApplicationRepository creditApplicationRepository;
  private final LoanAmortizationService amortizationService;
  private final ExchangeRateProvider exchangeRateProvider;

  @Override
  @Transactional
  public CreditApplication create(CreateCreditApplicationCommand command) {
    CreditApplication credit = CreditApplication.create(
        command.customerName(),
        command.customerEmail(),
        command.customerRfc(),
        command.requestedAmount(),
        command.currency(),
        command.termMonths(),
        command.annualInterestRate()
    );

    credit.calculatePayments(amortizationService);

    ExchangeRates exchangeRates = exchangeRateProvider.getRate(
        credit.getCurrency().name(),
        credit.getCurrency().otherCurrencies());

    credit.calculateExchangeAmounts(exchangeRates);

    return creditApplicationRepository.save(credit);
  }

  @Override
  @Transactional(readOnly = true)
  public CreditApplication get(UUID creditApplicationId) {
    return creditApplicationRepository.findById(creditApplicationId)
        .orElseThrow(() -> new ResourceNotFoundException("CreditApplication", creditApplicationId));
  }

  @Override
  @Transactional(readOnly = true)
  public PageResult<CreditApplication> execute(SearchCreditApplicationsQuery query) {
    return creditApplicationRepository.search(query);
  }

  @Override
  @Transactional
  public CreditApplication execute(ChangeCreditApplicationStatusCommand command) {
    CreditApplication credit = creditApplicationRepository.findById(command.creditApplicationId())
        .orElseThrow(() -> new ResourceNotFoundException("CreditApplication", command.creditApplicationId()));
    credit.changeStatus(command.status(), command.reason());
    return creditApplicationRepository.save(credit);
  }
}
