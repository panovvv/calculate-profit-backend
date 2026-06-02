package com.dachser.profit.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.dachser.profit.domain.model.Cost;
import com.dachser.profit.domain.model.CostType;
import com.dachser.profit.domain.model.Income;
import com.dachser.profit.domain.model.ProfitResult;
import com.dachser.profit.domain.service.ProfitCalculator;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Unit tests for the core business rule FR3: {@code Profit = Total Income - Total Costs}. */
class ProfitCalculatorTest {

  private final ProfitCalculator calculator = new ProfitCalculator();

  @Test
  @DisplayName("computes a profit when income exceeds costs (UI mockup: 1000 - 200 = 800)")
  void computesProfit() {
    ProfitResult result =
        calculator.calculate(
            List.of(Income.of(new BigDecimal("1000"))),
            List.of(Cost.of(new BigDecimal("200"), CostType.MAIN)));

    assertThat(result.totalIncome()).isEqualByComparingTo("1000.00");
    assertThat(result.totalCost()).isEqualByComparingTo("200.00");
    assertThat(result.profitOrLoss()).isEqualByComparingTo("800.00");
    assertThat(result.isProfit()).isTrue();
  }

  @Test
  @DisplayName("computes a loss when costs exceed income (UI mockup: 500 - 900 = -400)")
  void computesLoss() {
    ProfitResult result =
        calculator.calculate(
            List.of(Income.of(new BigDecimal("500"))),
            List.of(Cost.of(new BigDecimal("900"), CostType.MAIN)));

    assertThat(result.profitOrLoss()).isEqualByComparingTo("-400.00");
    assertThat(result.isProfit()).isFalse();
  }

  @Test
  @DisplayName("sums multiple costs of different types")
  void sumsMultipleCosts() {
    ProfitResult result =
        calculator.calculate(
            List.of(Income.of(new BigDecimal("1500"))),
            List.of(
                Cost.of(new BigDecimal("900"), CostType.MAIN),
                Cost.of(new BigDecimal("150.50"), CostType.ADDITIONAL)));

    assertThat(result.totalCost()).isEqualByComparingTo("1050.50");
    assertThat(result.profitOrLoss()).isEqualByComparingTo("449.50");
  }

  @Test
  @DisplayName("normalises amounts to two decimal places using HALF_UP rounding")
  void roundsToTwoDecimals() {
    ProfitResult result =
        calculator.calculate(
            List.of(Income.of(new BigDecimal("10.005"))),
            List.of(Cost.of(new BigDecimal("0.001"), CostType.MAIN)));

    assertThat(result.totalIncome()).isEqualByComparingTo("10.01");
    assertThat(result.totalCost()).isEqualByComparingTo("0.00");
    assertThat(result.profitOrLoss()).isEqualByComparingTo("10.01");
  }

  @Test
  @DisplayName("treats empty incomes and costs as zero (break-even)")
  void handlesEmptyInputs() {
    ProfitResult result = calculator.calculate(List.of(), List.of());

    assertThat(result.totalIncome()).isEqualByComparingTo("0.00");
    assertThat(result.profitOrLoss()).isEqualByComparingTo("0.00");
    assertThat(result.isProfit()).isTrue();
  }
}
