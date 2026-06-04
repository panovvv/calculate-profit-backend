package com.dachser.profit.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.dachser.profit.application.command.CalculateProfitCommand;
import com.dachser.profit.application.port.outgoing.ShipmentRepository;
import com.dachser.profit.domain.model.CostType;
import com.dachser.profit.domain.model.ProfitCalculation;
import com.dachser.profit.testsupport.ProfitTestData;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

/**
 * Context test for the application service against real collaborators (no mocks): the autowired
 * {@link ProfitService} runs through the real persistence adapters and H2. Each test uses a unique
 * shipment reference, so methods stay isolated without cleanup.
 */
@SpringBootTest
class ProfitServiceContextTest {

  @Autowired private ProfitService service;

  @Autowired private ShipmentRepository shipmentRepository;

  @Test
  void calculatePersistsShipmentWithCostRowsAndStoresTheResult() {
    String reference = ProfitTestData.uniqueReference();
    CalculateProfitCommand command =
        new CalculateProfitCommand(
            reference, new BigDecimal("1000"), new BigDecimal("200"), new BigDecimal("50"));

    ProfitCalculation stored = service.calculate(command);

    // Result: 1000 - (200 + 50) = 750.
    assertThat(stored.shipmentReference()).isEqualTo(reference);
    assertThat(stored.result().totalIncome()).isEqualByComparingTo("1000.00");
    assertThat(stored.result().totalCost()).isEqualByComparingTo("250.00");
    assertThat(stored.result().profitOrLoss()).isEqualByComparingTo("750.00");

    // The entered income/costs were persisted against the shipment (MAIN + ADDITIONAL).
    assertThat(shipmentRepository.findByReference(reference))
        .hasValueSatisfying(
            shipment -> {
              assertThat(shipment.incomes()).hasSize(1);
              assertThat(shipment.costs())
                  .extracting(c -> c.type())
                  .containsExactlyInAnyOrder(CostType.MAIN, CostType.ADDITIONAL);
            });
  }

  @Test
  void omitsTheAdditionalCostRowWhenZero() {
    String reference = ProfitTestData.uniqueReference();

    service.calculate(
        new CalculateProfitCommand(
            reference, new BigDecimal("500"), new BigDecimal("900"), BigDecimal.ZERO));

    assertThat(shipmentRepository.findByReference(reference))
        .hasValueSatisfying(
            shipment ->
                assertThat(shipment.costs())
                    .extracting(c -> c.type())
                    .containsExactly(CostType.MAIN));
  }

  @Test
  void historyForReturnsOnlyTheRequestedShipment() {
    String reference = ProfitTestData.uniqueReference();
    service.calculate(
        new CalculateProfitCommand(
            reference, new BigDecimal("300"), new BigDecimal("100"), BigDecimal.ZERO));

    Page<ProfitCalculation> history = service.historyFor(reference, PageRequest.of(0, 10));

    assertThat(history.getContent())
        .singleElement()
        .satisfies(
            c -> {
              assertThat(c.shipmentReference()).isEqualTo(reference);
              assertThat(c.result().profitOrLoss()).isEqualByComparingTo("200.00");
            });
    // The global (paged) history is populated and reports a total count.
    assertThat(service.history(PageRequest.of(0, 5)).getTotalElements()).isGreaterThanOrEqualTo(1);
  }
}
