package com.dmx.creditapplication.domain.model.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CurrencyTest {

  @Test
  void should_return_other_currencies_when_currecny_is_mxn() {
    // Arrange
    var currency = Currency.MXN;

    // Act
    String[] result = currency.otherCurrencies();

    // Assert
    assertEquals(2, result.length);

    assertArrayEquals(
        new String[]{"USD", "EUR"},
        result
    );
  }

  @Test
  void should_return_other_currencies_when_currecny_is_usd() {
    // Arrange
    var currency = Currency.USD;

    // Act
    String[] result = currency.otherCurrencies();

    // Assert
    assertEquals(2, result.length);

    assertArrayEquals(
        new String[]{"MXN", "EUR"},
        result
    );
  }

  @Test
  void should_return_other_currencies_when_currecny_is_eur() {
    // Arrange
    var currency = Currency.EUR;

    // Act
    String[] result = currency.otherCurrencies();

    // Assert
    assertEquals(2, result.length);

    assertArrayEquals(
        new String[]{"MXN", "USD"},
        result
    );
  }



}
