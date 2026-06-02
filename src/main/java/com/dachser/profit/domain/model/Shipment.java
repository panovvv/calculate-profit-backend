package com.dachser.profit.domain.model;

import java.util.List;
import java.util.Objects;

/**
 * Aggregate root for the profit use case: a shipment together with the incomes and costs recorded
 * against it. Profit/loss is derived from these by {@link
 * com.dachser.profit.domain.service.ProfitCalculator}.
 *
 * <p>The collections are defensively copied and exposed as unmodifiable lists, keeping the
 * aggregate immutable. {@code id} is {@code null} until the shipment has been persisted; {@code
 * reference} is the business identifier shown in the UI (e.g. {@code "0001"}).
 */
public record Shipment(
    Long id, String reference, String description, List<Income> incomes, List<Cost> costs) {

  public Shipment {
    Objects.requireNonNull(reference, "shipment reference must not be null");
    if (reference.isBlank()) {
      throw new IllegalArgumentException("shipment reference must not be blank");
    }
    incomes = incomes == null ? List.of() : List.copyOf(incomes);
    costs = costs == null ? List.of() : List.copyOf(costs);
  }

  /** Factory for a not-yet-persisted shipment carrying the entered incomes and costs. */
  public static Shipment of(
      String reference, String description, List<Income> incomes, List<Cost> costs) {
    return new Shipment(null, reference, description, incomes, costs);
  }
}
