package com.dachser.profit.application.port.incoming;

import com.dachser.profit.application.command.CalculateProfitCommand;
import com.dachser.profit.domain.model.ProfitCalculation;

/**
 * Incoming port (driving side) for the "Calculate Profit" use case.
 *
 * <p>Records the entered income and costs against the shipment, computes the profit or loss, stores
 * the result and returns it. Incoming adapters (e.g. the REST controller) depend on this interface
 * rather than on the concrete service.
 */
public interface CalculateProfitUseCase {

  /**
   * Executes the use case for a single calculation request.
   *
   * @param command the entered income/costs for a shipment
   * @return the persisted profit calculation (with identity and timestamp)
   */
  ProfitCalculation calculate(CalculateProfitCommand command);
}
