package com.dmx.creditapplication.domain.model.state;

import com.dmx.creditapplication.domain.model.enums.CreditStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CreditStatusTransitionsTest {

  @Test
  void should_allow_transition_from_created_to_under_review() {
    // Arrange
    CreditStatus from = CreditStatus.CREATED;
    CreditStatus to = CreditStatus.UNDER_REVIEW;

    // Act
    var result = CreditStatusTransitions.canTransition(from, to);

    // Assert
    assertTrue(result);
  }

  @Test
  void should_not_allow_transition_from_created_to_approved() {
    // Arrange
    CreditStatus from = CreditStatus.CREATED;
    CreditStatus to = CreditStatus.APPROVED;

    // Act
    boolean result = CreditStatusTransitions.canTransition(from, to);

    // Assert
    assertFalse(result);
  }

  @Test
  void should_not_allow_any_transition_from_rejected() {
    // Arrange
    CreditStatus from = CreditStatus.REJECTED;

    // Act & Assert
    for (CreditStatus to : CreditStatus.values()) {
      if (to != from) {
        boolean result = CreditStatusTransitions.canTransition(from, to);
        assertFalse(result);
      }
    }
  }

  @Test
  void should_not_allow_transition_from_approved() {

    // Arrange
    CreditStatus from = CreditStatus.APPROVED;

    // Act & Assert
    for (CreditStatus to : CreditStatus.values()) {
      if (to != from) {
        assertFalse(CreditStatusTransitions.canTransition(from, to));
      }
    }
  }

}
