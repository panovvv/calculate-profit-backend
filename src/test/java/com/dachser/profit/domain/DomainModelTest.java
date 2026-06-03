package com.dachser.profit.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.dachser.profit.application.command.CalculateProfitCommand;
import com.dachser.profit.domain.model.Cost;
import com.dachser.profit.domain.model.CostType;
import com.dachser.profit.domain.model.Income;
import com.dachser.profit.domain.model.ProfitResult;
import com.dachser.profit.domain.model.Shipment;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class DomainModelTest {

  @Test
  void incomeRejectsNegativeAndNullAmounts() {
    assertThatThrownBy(() -> Income.of(new BigDecimal("-0.01")))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> Income.of(null)).isInstanceOf(NullPointerException.class);
  }

  @Test
  void costRejectsNegativeAmountAndNullType() {
    assertThatThrownBy(() -> Cost.of(new BigDecimal("-1"), CostType.MAIN))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> Cost.of(new BigDecimal("1"), null))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void shipmentRejectsBlankOrNullReference() {
    assertThatThrownBy(() -> Shipment.of(" ", null, List.of(), List.of()))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> Shipment.of(null, null, List.of(), List.of()))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void profitResultIsProfitAtAndAboveZeroOnly() {
    assertThat(zeroResult("0.00").isProfit()).isTrue();
    assertThat(zeroResult("0.01").isProfit()).isTrue();
    assertThat(zeroResult("-0.01").isProfit()).isFalse();
  }

  @Test
  void commandDefaultsNullAdditionalCostToZero() {
    CalculateProfitCommand command =
        new CalculateProfitCommand("0001", new BigDecimal("10"), new BigDecimal("5"), null);
    assertThat(command.additionalCost()).isEqualByComparingTo("0");
  }

  private static ProfitResult zeroResult(String profitOrLoss) {
    return new ProfitResult(BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal(profitOrLoss));
  }
}
