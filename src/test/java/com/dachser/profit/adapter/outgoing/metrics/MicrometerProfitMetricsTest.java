package com.dachser.profit.adapter.outgoing.metrics;

import static org.assertj.core.api.Assertions.assertThat;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

/** Unit test for the Micrometer metrics adapter. */
class MicrometerProfitMetricsTest {

  @Test
  void incrementsCounterTaggedByEndpointAndStatus() {
    SimpleMeterRegistry registry = new SimpleMeterRegistry();
    MicrometerProfitMetrics metrics = new MicrometerProfitMetrics(registry);
    String endpoint = "ProfitController.calculate(..)";

    metrics.increment(endpoint, true);
    metrics.increment(endpoint, true);
    metrics.increment(endpoint, false);

    assertThat(
            registry.counter("profit.requests", "endpoint", endpoint, "status", "success").count())
        .isEqualTo(2.0);
    assertThat(
            registry.counter("profit.requests", "endpoint", endpoint, "status", "failure").count())
        .isEqualTo(1.0);
  }
}
