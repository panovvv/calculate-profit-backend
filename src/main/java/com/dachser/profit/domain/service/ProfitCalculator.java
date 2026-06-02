package com.dachser.profit.domain.service;

import com.dachser.profit.domain.model.Cost;
import com.dachser.profit.domain.model.Income;
import com.dachser.profit.domain.model.ProfitResult;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;

/**
 * Pure domain service implementing functional requirement FR3: {@code Profit = Total Income - Total
 * Costs}.
 *
 * <p>Stateless and framework-free so the core business rule can be unit-tested in isolation. All
 * monetary amounts are summed with {@link BigDecimal} and normalised to two decimal places using
 * {@link RoundingMode#HALF_UP}, matching the {@code DECIMAL(12,2)} storage precision.
 */
public class ProfitCalculator {

  private static final int MONEY_SCALE = 2;

  /**
   * Calculates the profit (or loss) for a shipment from its recorded incomes and costs.
   *
   * @param incomes the customer payments (the income side); may be empty
   * @param costs the service-provision costs (the cost side); may be empty
   * @return the totals and the resulting profit (positive) or loss (negative)
   */
  public ProfitResult calculate(Collection<Income> incomes, Collection<Cost> costs) {
    BigDecimal totalIncome =
        incomes.stream().map(Income::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
    BigDecimal totalCost =
        costs.stream().map(Cost::amount).reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal scaledIncome = scale(totalIncome);
    BigDecimal scaledCost = scale(totalCost);
    return new ProfitResult(scaledIncome, scaledCost, scaledIncome.subtract(scaledCost));
  }

  private static BigDecimal scale(BigDecimal value) {
    return value.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
  }
}
