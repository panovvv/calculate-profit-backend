package com.dachser.profit.application.command;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Input to the "Calculate Profit" use case, mirroring the fields captured by the UI form: a
 * shipment reference, the customer income, the main cost and an optional additional cost.
 *
 * <p>Validation of presence/format happens at the web boundary (Bean Validation on the request
 * DTO); this command carries already-parsed values into the application core.
 */
public record CalculateProfitCommand(
    String shipmentReference, BigDecimal income, BigDecimal cost, BigDecimal additionalCost) {

  public CalculateProfitCommand {
    Objects.requireNonNull(shipmentReference, "shipmentReference must not be null");
    Objects.requireNonNull(income, "income must not be null");
    Objects.requireNonNull(cost, "cost must not be null");
    // additionalCost is optional; default to zero when not provided.
    if (additionalCost == null) {
      additionalCost = BigDecimal.ZERO;
    }
  }
}
