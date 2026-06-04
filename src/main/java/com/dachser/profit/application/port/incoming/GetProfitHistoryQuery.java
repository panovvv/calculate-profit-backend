package com.dachser.profit.application.port.incoming;

import com.dachser.profit.domain.model.ProfitCalculation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Incoming port (driving side) for reading previously calculated profit/loss results, used to
 * populate the (paged) results grid in the UI.
 */
public interface GetProfitHistoryQuery {

  /** A page of stored calculations. */
  Page<ProfitCalculation> history(Pageable pageable);

  /** A page of stored calculations for a single shipment reference. */
  Page<ProfitCalculation> historyFor(String shipmentReference, Pageable pageable);
}
