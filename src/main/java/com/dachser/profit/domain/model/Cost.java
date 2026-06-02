package com.dachser.profit.domain.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * A cost incurred while servicing a shipment (the cost side of the profit equation).
 *
 * <p>Pure domain value object. {@code id} is {@code null} until the cost has been persisted.
 */
public record Cost(Long id, BigDecimal amount, CostType type) {

  public Cost {
    Objects.requireNonNull(amount, "cost amount must not be null");
    Objects.requireNonNull(type, "cost type must not be null");
    if (amount.signum() < 0) {
      throw new IllegalArgumentException("cost amount must not be negative: " + amount);
    }
  }

  /** Factory for a not-yet-persisted cost. */
  public static Cost of(BigDecimal amount, CostType type) {
    return new Cost(null, amount, type);
  }
}
