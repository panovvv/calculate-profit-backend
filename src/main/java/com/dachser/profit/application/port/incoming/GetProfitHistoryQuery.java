package com.dachser.profit.application.port.incoming;

import com.dachser.profit.domain.model.ProfitCalculation;
import java.util.List;

/**
 * Incoming port (driving side) for reading previously calculated profit/loss results, used to
 * populate the results grid in the UI.
 */
public interface GetProfitHistoryQuery {

  /** All stored calculations, most recent first. */
  List<ProfitCalculation> history();

  /** Stored calculations for a single shipment reference, most recent first. */
  List<ProfitCalculation> historyFor(String shipmentReference);
}
