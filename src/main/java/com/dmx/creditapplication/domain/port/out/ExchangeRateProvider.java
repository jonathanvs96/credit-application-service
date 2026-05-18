package com.dmx.creditapplication.domain.port.out;

import com.dmx.creditapplication.domain.model.entity.ExchangeRates;

public interface ExchangeRateProvider {

  ExchangeRates getRate(String baseCurrency, String ...targetCurrencies);

}
