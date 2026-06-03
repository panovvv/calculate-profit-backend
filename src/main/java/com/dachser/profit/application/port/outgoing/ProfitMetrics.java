package com.dachser.profit.application.port.outgoing;

/**
 * Outgoing port for recording application metrics, keeping the metrics backend (Micrometer) out of
 * the core. Implemented by an outgoing adapter; called from the web adapter's metrics aspect.
 */
public interface ProfitMetrics {

  /**
   * Records that a request to an endpoint completed.
   *
   * @param endpoint a short identifier for the handler (e.g. {@code
   *     ProfitController.calculate(..)})
   * @param success {@code true} if it returned normally, {@code false} if it threw
   */
  void increment(String endpoint, boolean success);
}
