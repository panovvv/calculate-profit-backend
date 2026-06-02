package com.dachser.profit.adapter.incoming.web.dto;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Response body for a profit calculation. The {@code income}, {@code totalCosts} and {@code
 * profitOrLoss} fields map directly onto the three columns of the UI results grid.
 *
 * @param id identity of the stored calculation
 * @param shipmentReference shipment the result belongs to
 * @param income total income (customer payments)
 * @param totalCosts sum of all recorded costs
 * @param profitOrLoss {@code income - totalCosts} (negative means a loss)
 * @param profit {@code true} when the shipment broke even or made a profit
 * @param calculatedAt when the calculation was performed and stored
 */
public record ProfitResponse(
    Long id,
    String shipmentReference,
    BigDecimal income,
    BigDecimal totalCosts,
    BigDecimal profitOrLoss,
    boolean profit,
    Instant calculatedAt) {}
