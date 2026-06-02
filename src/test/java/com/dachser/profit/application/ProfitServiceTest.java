package com.dachser.profit.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dachser.profit.application.command.CalculateProfitCommand;
import com.dachser.profit.application.port.outgoing.ProfitCalculationRepository;
import com.dachser.profit.application.port.outgoing.ShipmentRepository;
import com.dachser.profit.domain.model.CostType;
import com.dachser.profit.domain.model.ProfitCalculation;
import com.dachser.profit.domain.model.ProfitResult;
import com.dachser.profit.domain.model.Shipment;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Unit tests for the application service orchestration, with the outbound ports mocked. */
@ExtendWith(MockitoExtension.class)
class ProfitServiceTest {

  @Mock private ShipmentRepository shipmentRepository;

  @Mock private ProfitCalculationRepository profitCalculationRepository;

  @Test
  void calculatesPersistsShipmentAndStoresResult() {
    ProfitService service = new ProfitService(shipmentRepository, profitCalculationRepository);
    CalculateProfitCommand command =
        new CalculateProfitCommand(
            "0001", new BigDecimal("1000"), new BigDecimal("200"), new BigDecimal("50"));

    ProfitCalculation stored =
        new ProfitCalculation(
            7L,
            "0001",
            new ProfitResult(
                new BigDecimal("1000.00"), new BigDecimal("250.00"), new BigDecimal("750.00")),
            Instant.now());
    when(profitCalculationRepository.save(eq("0001"), any(ProfitResult.class))).thenReturn(stored);

    ProfitCalculation result = service.calculate(command);

    // The entered income/costs are persisted against the shipment (main + additional cost).
    ArgumentCaptor<Shipment> shipmentCaptor = ArgumentCaptor.forClass(Shipment.class);
    verify(shipmentRepository).save(shipmentCaptor.capture());
    Shipment savedShipment = shipmentCaptor.getValue();
    assertThat(savedShipment.reference()).isEqualTo("0001");
    assertThat(savedShipment.incomes()).hasSize(1);
    assertThat(savedShipment.costs())
        .extracting("type")
        .containsExactly(CostType.MAIN, CostType.ADDITIONAL);

    // The computed result (1000 - 250 = 750) is handed to the repository for storage.
    ArgumentCaptor<ProfitResult> resultCaptor = ArgumentCaptor.forClass(ProfitResult.class);
    verify(profitCalculationRepository).save(eq("0001"), resultCaptor.capture());
    assertThat(resultCaptor.getValue().profitOrLoss()).isEqualByComparingTo("750.00");

    assertThat(result).isSameAs(stored);
  }

  @Test
  void omitsAdditionalCostWhenZero() {
    ProfitService service = new ProfitService(shipmentRepository, profitCalculationRepository);
    CalculateProfitCommand command =
        new CalculateProfitCommand(
            "0002", new BigDecimal("500"), new BigDecimal("900"), BigDecimal.ZERO);
    when(profitCalculationRepository.save(eq("0002"), any(ProfitResult.class)))
        .thenReturn(
            new ProfitCalculation(
                1L,
                "0002",
                new ProfitResult(
                    new BigDecimal("500.00"), new BigDecimal("900.00"), new BigDecimal("-400.00")),
                Instant.now()));

    service.calculate(command);

    ArgumentCaptor<Shipment> shipmentCaptor = ArgumentCaptor.forClass(Shipment.class);
    verify(shipmentRepository).save(shipmentCaptor.capture());
    assertThat(shipmentCaptor.getValue().costs()).extracting("type").containsExactly(CostType.MAIN);
  }
}
