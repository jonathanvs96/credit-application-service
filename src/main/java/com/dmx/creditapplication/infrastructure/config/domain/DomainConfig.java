package com.dmx.creditapplication.infrastructure.config.domain;

import com.dmx.creditapplication.domain.service.calculation.LoanAmortizationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainConfig {

  @Bean
  public LoanAmortizationService loanAmortizationService() {
    return new LoanAmortizationService();
  }

}
