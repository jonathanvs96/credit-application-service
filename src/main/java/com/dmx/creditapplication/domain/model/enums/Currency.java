package com.dmx.creditapplication.domain.model.enums;

import java.util.Arrays;

public enum Currency {
  MXN,
  USD,
  EUR;

  public String[] otherCurrencies() {
    return Arrays.stream(values())
        .filter(currency -> currency != this)
        .map(Enum::name)
        .toArray(String[]::new);
  }
}
