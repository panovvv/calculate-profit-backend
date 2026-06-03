package com.dachser.profit.adapter.outgoing.persistence;

import com.dachser.profit.application.port.outgoing.ShipmentRepository;
import com.dachser.profit.domain.model.Cost;
import com.dachser.profit.domain.model.Income;
import com.dachser.profit.domain.model.Shipment;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Outgoing persistence adapter implementing {@link ShipmentRepository} on top of Spring Data JPA.
 * Translates between the pure domain {@link Shipment} aggregate and the JPA entities, keeping the
 * mapping (and all JPA types) confined to this adapter.
 *
 * <p>Methods are transactional so lazy associations are materialised while a session is open (when
 * called from the application service the calls simply join its transaction).
 */
@Component
@RequiredArgsConstructor
class ShipmentPersistenceAdapter implements ShipmentRepository {

  private final ShipmentJpaRepository repository;

  @Override
  @Transactional
  public Shipment save(Shipment shipment) {
    ShipmentJpaEntity entity =
        repository.findByReference(shipment.reference()).orElseGet(ShipmentJpaEntity::new);
    entity.setReference(shipment.reference());
    entity.setDescription(shipment.description());
    entity.replaceIncomes(
        shipment.incomes().stream().map(income -> new IncomeJpaEntity(income.amount())).toList());
    entity.replaceCosts(
        shipment.costs().stream()
            .map(cost -> new CostJpaEntity(cost.amount(), cost.type()))
            .toList());
    return toDomain(repository.save(entity));
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<Shipment> findByReference(String reference) {
    return repository.findByReference(reference).map(ShipmentPersistenceAdapter::toDomain);
  }

  private static Shipment toDomain(ShipmentJpaEntity entity) {
    List<Income> incomes =
        entity.getIncomes().stream()
            .map(income -> new Income(income.getId(), income.getAmount()))
            .toList();
    List<Cost> costs =
        entity.getCosts().stream()
            .map(cost -> new Cost(cost.getId(), cost.getAmount(), cost.getType()))
            .toList();
    return new Shipment(
        entity.getId(), entity.getReference(), entity.getDescription(), incomes, costs);
  }
}
