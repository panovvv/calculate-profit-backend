package com.dachser.profit.application.port.outgoing;

import com.dachser.profit.domain.model.Shipment;
import java.util.Optional;

/**
 * Outbound port (driven side) for persisting and loading shipments together with their incomes and
 * costs. Implemented by a persistence adapter; the application core depends only on this interface
 * (dependency inversion).
 */
public interface ShipmentRepository {

  /**
   * Creates or updates the shipment identified by its business reference, replacing its recorded
   * incomes and costs with those carried by the given aggregate.
   *
   * @return the persisted shipment, with database identities populated
   */
  Shipment save(Shipment shipment);

  Optional<Shipment> findByReference(String reference);
}
