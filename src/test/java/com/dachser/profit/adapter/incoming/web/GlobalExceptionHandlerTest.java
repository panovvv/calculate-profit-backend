package com.dachser.profit.adapter.incoming.web;

import static org.assertj.core.api.Assertions.assertThat;

import com.dachser.profit.adapter.incoming.web.GlobalExceptionHandler.ApiError;
import org.junit.jupiter.api.Test;

/**
 * Unit test for the unexpected-error (500) path: it must not leak internal exception details. The
 * 4xx responses are verified end-to-end in the REST Assured API tests.
 */
class GlobalExceptionHandlerTest {

  @Test
  void unexpectedErrorHidesInternalDetails() {
    ApiError error = new GlobalExceptionHandler().handleUnexpected(new RuntimeException("boom"));

    assertThat(error.detail()).doesNotContain("boom");
    assertThat(error.timestamp()).isNotNull();
  }
}
