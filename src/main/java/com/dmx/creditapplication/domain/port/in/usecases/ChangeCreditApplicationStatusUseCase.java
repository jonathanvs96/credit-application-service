package com.dmx.creditapplication.domain.port.in.usecases;

import com.dmx.creditapplication.domain.model.entity.CreditApplication;
import com.dmx.creditapplication.domain.port.in.command.ChangeCreditApplicationStatusCommand;

public interface ChangeCreditApplicationStatusUseCase {

  CreditApplication execute(ChangeCreditApplicationStatusCommand command);

}
