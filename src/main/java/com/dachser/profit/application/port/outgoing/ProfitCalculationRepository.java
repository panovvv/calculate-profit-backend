package com.dachser.profit.application.port.outgoing;

import com.dachser.profit.domain.model.ProfitCalculation;
import com.dachser.profit.domain.model.ProfitResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** Outgoing port (driven side) for storing and retrieving profit calculations. */
public interface ProfitCalculationRepository {

  /**
   * Stores a new profit calculation for a shipment. The implementation assigns the identity and
   * stamps the calculation instant.
   *
   * @param shipmentReference the shipment the result belongs to
   * @param result the computed totals and profit/loss
   * @return the persisted calculation
   */
  ProfitCalculation save(String shipmentReference, ProfitResult result);

  /** A page of stored calculations. */
  Page<ProfitCalculation> findAll(Pageable pageable);

  /** A page of stored calculations for one shipment reference. */
  Page<ProfitCalculation> findByShipmentReference(String shipmentReference, Pageable pageable);
}
