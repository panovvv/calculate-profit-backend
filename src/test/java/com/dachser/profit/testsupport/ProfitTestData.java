package com.dachser.profit.testsupport;

import com.dachser.profit.domain.model.Cost;
import com.dachser.profit.domain.model.CostType;
import com.dachser.profit.domain.model.Income;
import com.dachser.profit.domain.model.Shipment;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Hand-rolled factory for randomized, parallel-safe test data (no external libraries).
 *
 * <p>Every shipment gets a globally-unique reference via {@link UUID} — so tests never collide and
 * each can query strictly by its own reference, without seed data, cleanup or shared counters.
 * Monetary values use {@link ThreadLocalRandom} (per-thread, contention-free).
 */
public final class ProfitTestData {

  private ProfitTestData() {}

  /** A reference that is unique across the whole JVM run (no shared sequence/counter). */
  public static String uniqueReference() {
    return "T-" + UUID.randomUUID();
  }

  /** A non-negative amount in {@code [0.00, 9999.99]} with two decimal places. */
  public static BigDecimal randomMoney() {
    return BigDecimal.valueOf(ThreadLocalRandom.current().nextLong(0, 1_000_000), 2);
  }

  public static Income randomIncome() {
    return Income.of(randomMoney());
  }

  public static Cost randomCost(CostType type) {
    return Cost.of(randomMoney(), type);
  }

  /** A fully randomized shipment with a unique reference, one income and two costs. */
  public static Shipment randomShipment() {
    return Shipment.of(
        uniqueReference(),
        "randomized test shipment",
        List.of(randomIncome()),
        List.of(randomCost(CostType.MAIN), randomCost(CostType.ADDITIONAL)));
  }
}
