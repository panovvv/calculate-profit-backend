package com.dachser.profit.adapter.outgoing.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.dachser.profit.domain.model.Cost;
import com.dachser.profit.domain.model.CostType;
import com.dachser.profit.domain.model.Income;
import com.dachser.profit.domain.model.ProfitCalculation;
import com.dachser.profit.domain.model.ProfitResult;
import com.dachser.profit.domain.model.Shipment;
import com.dachser.profit.domain.service.ProfitCalculator;
import com.dachser.profit.testsupport.ProfitTestData;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Integration test for the outgoing persistence adapters against real H2. Booting the full context
 * also verifies that the Flyway migrations apply and that the JPA mappings pass Hibernate's {@code
 * ddl-auto=validate} check against the Flyway-created schema.
 *
 * <p>Each test creates its own data under a unique shipment reference ({@link ProfitTestData}) and
 * asserts only on that reference, so the tests are parallel-safe with no seed data, no
 * transactional rollback and no cleanup — they never observe each other's rows.
 */
@SpringBootTest
class PersistenceAdapterTest {

  private final ProfitCalculator calculator = new ProfitCalculator();

  @Autowired private ShipmentPersistenceAdapter shipmentAdapter;

  @Autowired private ProfitPersistenceAdapter profitAdapter;

  @Test
  @DisplayName("saving a shipment persists its incomes and costs and is retrievable by reference")
  void savesAndLoadsShipment() {
    Shipment shipment = ProfitTestData.randomShipment();

    shipmentAdapter.save(shipment);

    assertThat(shipmentAdapter.findByReference(shipment.reference()))
        .hasValueSatisfying(
            loaded -> {
              assertThat(loaded.incomes()).hasSize(1);
              assertThat(loaded.costs()).hasSize(2);
            });
  }

  @Test
  @DisplayName(
      "re-saving the same reference replaces incomes/costs rather than duplicating the shipment")
  void upsertsByReference() {
    String reference = ProfitTestData.uniqueReference();
    shipmentAdapter.save(
        Shipment.of(
            reference,
            null,
            List.of(Income.of(new BigDecimal("100.00"))),
            List.of(Cost.of(new BigDecimal("10.00"), CostType.MAIN))));
    shipmentAdapter.save(
        Shipment.of(
            reference,
            null,
            List.of(Income.of(new BigDecimal("200.00"))),
            List.of(Cost.of(new BigDecimal("20.00"), CostType.MAIN))));

    assertThat(shipmentAdapter.findByReference(reference))
        .hasValueSatisfying(
            loaded -> {
              assertThat(loaded.incomes())
                  .singleElement()
                  .satisfies(i -> assertThat(i.amount()).isEqualByComparingTo("200.00"));
              assertThat(loaded.costs()).hasSize(1);
            });
  }

  @Test
  @DisplayName(
      "storing a calculation links it to the shipment, stamps the instant, and is queryable")
  void storesAndRetrievesCalculation() {
    Shipment shipment = ProfitTestData.randomShipment();
    shipmentAdapter.save(shipment);
    ProfitResult result = calculator.calculate(shipment.incomes(), shipment.costs());

    ProfitCalculation stored = profitAdapter.save(shipment.reference(), result);

    assertThat(stored.id()).isNotNull();
    assertThat(stored.shipmentReference()).isEqualTo(shipment.reference());
    assertThat(stored.calculatedAt()).isNotNull();
    assertThat(stored.result().profitOrLoss()).isEqualByComparingTo(result.profitOrLoss());

    // Scoped query: returns only this shipment's data, regardless of what other tests stored.
    assertThat(profitAdapter.findByShipmentReference(shipment.reference()))
        .singleElement()
        .satisfies(
            c -> assertThat(c.result().profitOrLoss()).isEqualByComparingTo(result.profitOrLoss()));
  }

  @Test
  @DisplayName("findAll contains this test's stored calculation (no reliance on a global count)")
  void findAllContainsOwnCalculation() {
    Shipment shipment = ProfitTestData.randomShipment();
    shipmentAdapter.save(shipment);
    profitAdapter.save(
        shipment.reference(), calculator.calculate(shipment.incomes(), shipment.costs()));

    assertThat(profitAdapter.findAll())
        .anySatisfy(c -> assertThat(c.shipmentReference()).isEqualTo(shipment.reference()));
  }
}
