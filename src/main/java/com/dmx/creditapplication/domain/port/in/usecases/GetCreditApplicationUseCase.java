package com.dmx.creditapplication.domain.port.in.usecases;

import com.dmx.creditapplication.domain.model.entity.CreditApplication;

import java.util.UUID;

public interface GetCreditApplicationUseCase {

  CreditApplication get(UUID creditApplicationId);

}
