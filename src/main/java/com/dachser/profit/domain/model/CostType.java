package com.dachser.profit.domain.model;

/**
 * Classifies a cost incurred while servicing a shipment.
 *
 * <p>The UI captures a primary {@link #MAIN} cost and an optional {@link #ADDITIONAL} cost, but the
 * domain treats any number of costs of either type uniformly when summing total costs.
 */
public enum CostType {
  /** The principal service cost of the main carriage. */
  MAIN,
  /** Any extra cost on top of the main carriage (surcharges, handling, etc.). */
  ADDITIONAL
}
