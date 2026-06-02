package com.dachser.profit.adapter.incoming.web;

import com.dachser.profit.adapter.incoming.web.dto.CalculateProfitRequest;
import com.dachser.profit.adapter.incoming.web.dto.ProfitResponse;
import com.dachser.profit.application.command.CalculateProfitCommand;
import com.dachser.profit.application.port.incoming.CalculateProfitUseCase;
import com.dachser.profit.application.port.incoming.GetProfitHistoryQuery;
import com.dachser.profit.domain.model.ProfitCalculation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * The single inbound REST adapter for the profit use case (the brief asks for one controller).
 *
 * <ul>
 *   <li>{@code POST /api/profit/calculations} — calculate and store the profit/loss for a shipment.
 *   <li>{@code GET /api/profit/calculations} — list stored calculations for the results grid,
 *       optionally filtered by shipment reference.
 * </ul>
 *
 * The controller depends only on the inbound ports and the web mapper; all business logic lives in
 * the application core.
 */
@RestController
@RequestMapping("/api/profit/calculations")
@Tag(name = SwaggerApiTags.PROFIT)
@RequiredArgsConstructor
public class ProfitController {

  private final CalculateProfitUseCase calculateProfitUseCase;
  private final GetProfitHistoryQuery getProfitHistoryQuery;
  private final ProfitWebMapper mapper;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(
      summary = "Calculate profit",
      description =
          "Records the entered income and costs for a shipment, computes the profit or loss,"
              + " stores it and returns the result.")
  @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Calculation performed and stored"),
    @ApiResponse(
        responseCode = "400",
        description = "Invalid request (e.g. missing or negative amounts)"),
    @ApiResponse(responseCode = "422", description = "Request violated a domain rule")
  })
  public ProfitResponse calculate(@Valid @RequestBody CalculateProfitRequest request) {
    CalculateProfitCommand command =
        new CalculateProfitCommand(
            request.shipmentReference(),
            request.income(),
            request.cost(),
            request.additionalCost());
    ProfitCalculation calculation = calculateProfitUseCase.calculate(command);
    return mapper.toResponse(calculation);
  }

  @GetMapping
  @Operation(
      summary = "List calculations",
      description = "Lists stored profit calculations, optionally filtered by shipment reference.")
  public List<ProfitResponse> list(
      @RequestParam(name = "shipment", required = false) String shipmentReference) {
    List<ProfitCalculation> calculations =
        (shipmentReference == null || shipmentReference.isBlank())
            ? getProfitHistoryQuery.history()
            : getProfitHistoryQuery.historyFor(shipmentReference);
    return mapper.toResponses(calculations);
  }
}
