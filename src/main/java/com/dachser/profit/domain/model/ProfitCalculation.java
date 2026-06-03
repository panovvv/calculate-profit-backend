package com.dachser.profit.domain.model;

import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * A persisted profit calculation: the {@link ProfitResult} for a shipment plus its identity and the
 * timezone-aware instant it was calculated. This is what the "Calculate Profit" post-condition
 * stores and what the results grid in the UI lists.
 */
public record ProfitCalculation(
    Long id, String shipmentReference, ProfitResult result, OffsetDateTime calculatedAt) {

  public ProfitCalculation {
    Objects.requireNonNull(shipmentReference, "shipmentReference must not be null");
    Objects.requireNonNull(result, "result must not be null");
    Objects.requireNonNull(calculatedAt, "calculatedAt must not be null");
  }
}
