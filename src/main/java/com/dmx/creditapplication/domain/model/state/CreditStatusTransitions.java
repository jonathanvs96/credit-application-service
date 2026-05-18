package com.dmx.creditapplication.domain.model.state;

import com.dmx.creditapplication.domain.model.enums.CreditStatus;

import java.util.Map;
import java.util.Set;

public class CreditStatusTransitions {

  private static final Map<CreditStatus, Set<CreditStatus>> ALLOWED_TRANSITIONS = Map.of(
      CreditStatus.CREATED, Set.of(
          CreditStatus.UNDER_REVIEW,
          CreditStatus.CANCELLED
      ),

      CreditStatus.UNDER_REVIEW, Set.of(
          CreditStatus.APPROVED,
          CreditStatus.REJECTED,
          CreditStatus.CANCELLED
      ),

      CreditStatus.APPROVED, Set.of(),
      CreditStatus.REJECTED, Set.of(),
      CreditStatus.CANCELLED, Set.of()
  );

  public static boolean canTransition(CreditStatus from, CreditStatus to) {
    return ALLOWED_TRANSITIONS
        .getOrDefault(from, Set.of())
        .contains(to);
  }

}
