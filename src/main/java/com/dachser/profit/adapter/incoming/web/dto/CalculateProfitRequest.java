package com.dachser.profit.adapter.incoming.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

/**
 * Request body for the "Calculate Profit" endpoint, mirroring the UI form fields. Bean Validation
 * constraints reject malformed input at the web boundary before it reaches the application core.
 *
 * @param shipmentReference business identifier of the shipment (e.g. {@code "0001"})
 * @param income customer payment (income side); must be zero or positive
 * @param cost main service cost; must be zero or positive
 * @param additionalCost optional extra cost; defaults to zero when omitted
 */
public record CalculateProfitRequest(
    @Schema(description = "Business identifier of the shipment", example = "0001") @NotBlank
        String shipmentReference,
    @Schema(description = "Customer payment (income side)", example = "1000.00")
        @NotNull
        @PositiveOrZero
        @Digits(integer = 10, fraction = 2)
        BigDecimal income,
    @Schema(description = "Main service cost", example = "200.00")
        @NotNull
        @PositiveOrZero
        @Digits(integer = 10, fraction = 2)
        BigDecimal cost,
    @Schema(description = "Optional extra cost; defaults to zero", example = "0.00")
        @PositiveOrZero
        @Digits(integer = 10, fraction = 2)
        BigDecimal additionalCost) {}
