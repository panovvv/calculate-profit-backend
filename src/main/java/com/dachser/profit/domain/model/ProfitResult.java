package com.dachser.profit.domain.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Immutable outcome of the profit calculation for a shipment: {@code profitOrLoss = totalIncome -
 * totalCost}.
 *
 * <p>A non-negative {@link #profitOrLoss()} is a profit; a negative value is a loss. All amounts
 * are normalised to two decimal places by {@link
 * com.dachser.profit.domain.service.ProfitCalculator}.
 */
public record ProfitResult(BigDecimal totalIncome, BigDecimal totalCost, BigDecimal profitOrLoss) {

  public ProfitResult {
    Objects.requireNonNull(totalIncome, "totalIncome must not be null");
    Objects.requireNonNull(totalCost, "totalCost must not be null");
    Objects.requireNonNull(profitOrLoss, "profitOrLoss must not be null");
  }

  /** {@code true} when the shipment broke even or made a profit. */
  public boolean isProfit() {
    return profitOrLoss.signum() >= 0;
  }
}
