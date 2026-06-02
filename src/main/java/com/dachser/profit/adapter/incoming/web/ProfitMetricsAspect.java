package com.dachser.profit.adapter.incoming.web;

import com.dachser.profit.application.port.outgoing.ProfitMetrics;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/**
 * Records a success/failure metric for every REST controller invocation, keeping the controllers
 * free of metrics code. Depends only on the {@link ProfitMetrics} port, not on the Micrometer
 * adapter.
 */
@Aspect
@Component
@RequiredArgsConstructor
class ProfitMetricsAspect {

  private final ProfitMetrics metrics;

  @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
  void restControllerMethods() {}

  @AfterReturning("restControllerMethods()")
  void onSuccess(JoinPoint joinPoint) {
    metrics.increment(joinPoint.getSignature().toShortString(), true);
  }

  @AfterThrowing("restControllerMethods()")
  void onError(JoinPoint joinPoint) {
    metrics.increment(joinPoint.getSignature().toShortString(), false);
  }
}
