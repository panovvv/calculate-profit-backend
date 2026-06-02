package com.dachser.profit.adapter.incoming.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dachser.profit.application.command.CalculateProfitCommand;
import com.dachser.profit.application.port.incoming.CalculateProfitUseCase;
import com.dachser.profit.application.port.incoming.GetProfitHistoryQuery;
import com.dachser.profit.domain.model.ProfitCalculation;
import com.dachser.profit.domain.model.ProfitResult;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 * Web-layer test for {@link ProfitController}: request mapping, validation, error handling and JSON
 * shape. Uses a standalone {@link MockMvc} (no Spring context) with the application core mocked and
 * the real (generated) MapStruct mapper, keeping the test fast and decoupled from Spring Boot's
 * test slices.
 */
class ProfitControllerTest {

  private final CalculateProfitUseCase calculateProfitUseCase = mock(CalculateProfitUseCase.class);
  private final GetProfitHistoryQuery getProfitHistoryQuery = mock(GetProfitHistoryQuery.class);

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    ProfitController controller =
        new ProfitController(
            calculateProfitUseCase, getProfitHistoryQuery, new ProfitWebMapperImpl());
    mockMvc =
        MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
  }

  @Test
  void calculateReturnsCreatedWithProfitBreakdown() throws Exception {
    ProfitCalculation stored =
        new ProfitCalculation(
            1L,
            "0001",
            new ProfitResult(
                new BigDecimal("1000.00"), new BigDecimal("200.00"), new BigDecimal("800.00")),
            Instant.parse("2026-06-01T10:15:30Z"));
    when(calculateProfitUseCase.calculate(any(CalculateProfitCommand.class))).thenReturn(stored);

    mockMvc
        .perform(
            post("/api/profit/calculations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                                {"shipmentReference":"0001","income":1000,"cost":200,"additionalCost":0}"""))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.shipmentReference").value("0001"))
        .andExpect(jsonPath("$.income").value(1000.00))
        .andExpect(jsonPath("$.totalCosts").value(200.00))
        .andExpect(jsonPath("$.profitOrLoss").value(800.00))
        .andExpect(jsonPath("$.profit").value(true));
  }

  @Test
  void calculateRejectsInvalidRequestWith400() throws Exception {
    mockMvc
        .perform(
            post("/api/profit/calculations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                                {"shipmentReference":"","income":-5}"""))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.detail").isNotEmpty());
  }

  @Test
  void listReturnsStoredCalculations() throws Exception {
    when(getProfitHistoryQuery.history())
        .thenReturn(
            List.of(
                new ProfitCalculation(
                    1L,
                    "0001",
                    new ProfitResult(
                        new BigDecimal("1000.00"),
                        new BigDecimal("200.00"),
                        new BigDecimal("800.00")),
                    Instant.parse("2026-06-01T10:15:30Z"))));

    mockMvc
        .perform(get("/api/profit/calculations"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].profitOrLoss").value(800.00));
  }
}
