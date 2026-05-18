package com.dmx.creditapplication.infrastructure.config.cache;

import java.util.Arrays;

public final class CacheKeys {

  private CacheKeys(){}

  public static String exchangeRateKey(
      String baseCurrency,
      String[] targetCurencies){
    String[] copy = Arrays.copyOf(targetCurencies, targetCurencies.length);
    Arrays.sort(copy);
    return baseCurrency+"-"+String.join(",",copy);

  }

}
