package com.dachser.profit.application.port.outgoing;

import com.dachser.profit.domain.model.ProfitCalculation;
import com.dachser.profit.domain.model.ProfitResult;
import java.util.List;

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

  /** All stored calculations, most recent first. */
  List<ProfitCalculation> findAll();

  /** Stored calculations for one shipment reference, most recent first. */
  List<ProfitCalculation> findByShipmentReference(String shipmentReference);
}
