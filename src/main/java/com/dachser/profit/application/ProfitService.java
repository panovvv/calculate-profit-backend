package com.dachser.profit.application;

import com.dachser.profit.application.command.CalculateProfitCommand;
import com.dachser.profit.application.port.incoming.CalculateProfitUseCase;
import com.dachser.profit.application.port.incoming.GetProfitHistoryQuery;
import com.dachser.profit.application.port.outgoing.ProfitCalculationRepository;
import com.dachser.profit.application.port.outgoing.ShipmentRepository;
import com.dachser.profit.domain.model.Cost;
import com.dachser.profit.domain.model.CostType;
import com.dachser.profit.domain.model.Income;
import com.dachser.profit.domain.model.ProfitCalculation;
import com.dachser.profit.domain.model.ProfitResult;
import com.dachser.profit.domain.model.Shipment;
import com.dachser.profit.domain.service.ProfitCalculator;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service orchestrating the "Calculate Profit" use case. It is the single
 * implementation of the incoming ports and depends only on outgoing ports and the pure domain —
 * never on adapters.
 *
 * <p>Flow (mirrors the use-case main flow): build the entered incomes/costs into a {@link
 * Shipment}, persist it, compute the profit/loss via {@link ProfitCalculator}, store the result,
 * and return it.
 */
@Service
@RequiredArgsConstructor
public class ProfitService implements CalculateProfitUseCase, GetProfitHistoryQuery {

  private final ShipmentRepository shipmentRepository;
  private final ProfitCalculationRepository profitCalculationRepository;

  // Initialised inline (pure, stateless) so Lombok's @RequiredArgsConstructor leaves it out.
  private final ProfitCalculator profitCalculator = new ProfitCalculator();

  @Override
  @Transactional
  public ProfitCalculation calculate(CalculateProfitCommand command) {
    List<Income> incomes = List.of(Income.of(command.income()));
    List<Cost> costs = buildCosts(command);

    // Step 2 & 3: store the entered income and cost data for the shipment.
    shipmentRepository.save(Shipment.of(command.shipmentReference(), null, incomes, costs));

    // Step 4: calculate profit or loss (pure domain rule).
    ProfitResult result = profitCalculator.calculate(incomes, costs);

    // Step 5: store the calculated result.
    return profitCalculationRepository.save(command.shipmentReference(), result);
  }

  @Override
  @Transactional(readOnly = true)
  public List<ProfitCalculation> history() {
    return profitCalculationRepository.findAll();
  }

  @Override
  @Transactional(readOnly = true)
  public List<ProfitCalculation> historyFor(String shipmentReference) {
    return profitCalculationRepository.findByShipmentReference(shipmentReference);
  }

  private static List<Cost> buildCosts(CalculateProfitCommand command) {
    List<Cost> costs = new ArrayList<>();
    costs.add(Cost.of(command.cost(), CostType.MAIN));
    if (command.additionalCost().compareTo(BigDecimal.ZERO) > 0) {
      costs.add(Cost.of(command.additionalCost(), CostType.ADDITIONAL));
    }
    return costs;
  }
}
