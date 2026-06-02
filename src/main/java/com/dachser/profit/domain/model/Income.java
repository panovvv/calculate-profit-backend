package com.dachser.profit.domain.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * A customer payment recorded against a shipment (the income side of the profit equation).
 *
 * <p>Pure domain value object: monetary values use {@link BigDecimal} (never {@code double}) to
 * avoid binary floating-point rounding errors. {@code id} is {@code null} until the income has been
 * persisted.
 */
public record Income(Long id, BigDecimal amount) {

  public Income {
    Objects.requireNonNull(amount, "income amount must not be null");
    if (amount.signum() < 0) {
      throw new IllegalArgumentException("income amount must not be negative: " + amount);
    }
  }

  /** Factory for a not-yet-persisted income. */
  public static Income of(BigDecimal amount) {
    return new Income(null, amount);
  }
}
