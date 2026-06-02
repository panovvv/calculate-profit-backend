package com.dachser.profit.adapter.outgoing.metrics;

import com.dachser.profit.application.port.outgoing.ProfitMetrics;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Outbound adapter implementing {@link ProfitMetrics} with Micrometer. Emits a {@code
 * profit.requests} counter tagged by endpoint and outcome, which Actuator exposes (e.g. at {@code
 * /actuator/prometheus}).
 */
@Component
@RequiredArgsConstructor
class MicrometerProfitMetrics implements ProfitMetrics {

  private static final String COUNTER = "profit.requests";

  private final MeterRegistry registry;

  @Override
  public void increment(String endpoint, boolean success) {
    registry
        .counter(COUNTER, "endpoint", endpoint, "status", success ? "success" : "failure")
        .increment();
  }
}
