package com.dmx.creditapplication.domain.port.in.command;

import com.dmx.creditapplication.domain.model.enums.CreditStatus;

import java.util.UUID;

public record ChangeCreditApplicationStatusCommand(
    UUID creditApplicationId,
    CreditStatus status,
    String reason
) {
}
