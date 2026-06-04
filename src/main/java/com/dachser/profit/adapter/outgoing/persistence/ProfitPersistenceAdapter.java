package com.dachser.profit.adapter.outgoing.persistence;

import com.dachser.profit.application.port.outgoing.ProfitCalculationRepository;
import com.dachser.profit.domain.model.ProfitCalculation;
import com.dachser.profit.domain.model.ProfitResult;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Outgoing persistence adapter implementing {@link ProfitCalculationRepository} on top of Spring
 * Data JPA. Links each stored calculation to its shipment by reference; the calculation instant is
 * a timezone-aware {@link OffsetDateTime} (stamped in UTC).
 *
 * <p>Methods are transactional so the lazy {@code shipment} association is materialised while a
 * session is open (when called from the application service the calls simply join its transaction).
 */
@Component
@RequiredArgsConstructor
class ProfitPersistenceAdapter implements ProfitCalculationRepository {

  private final ProfitCalculationJpaRepository repository;
  private final ShipmentJpaRepository shipmentRepository;

  @Override
  @Transactional
  public ProfitCalculation save(String shipmentReference, ProfitResult result) {
    ShipmentJpaEntity shipment =
        shipmentRepository
            .findByReference(shipmentReference)
            .orElseThrow(
                () ->
                    new IllegalStateException(
                        "Cannot store profit calculation: unknown shipment reference "
                            + shipmentReference));

    ProfitCalculationJpaEntity entity = new ProfitCalculationJpaEntity();
    entity.setShipment(shipment);
    entity.setTotalIncome(result.totalIncome());
    entity.setTotalCost(result.totalCost());
    entity.setProfitOrLoss(result.profitOrLoss());
    entity.setCalculatedAt(OffsetDateTime.now(ZoneOffset.UTC));
    return toDomain(repository.save(entity));
  }

  @Override
  @Transactional(readOnly = true)
  public Page<ProfitCalculation> findAll(Pageable pageable) {
    return repository.findAll(pageable).map(ProfitPersistenceAdapter::toDomain);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<ProfitCalculation> findByShipmentReference(
      String shipmentReference, Pageable pageable) {
    return repository
        .findByShipmentReference(shipmentReference, pageable)
        .map(ProfitPersistenceAdapter::toDomain);
  }

  private static ProfitCalculation toDomain(ProfitCalculationJpaEntity entity) {
    ProfitResult result =
        new ProfitResult(entity.getTotalIncome(), entity.getTotalCost(), entity.getProfitOrLoss());
    return new ProfitCalculation(
        entity.getId(), entity.getShipment().getReference(), result, entity.getCalculatedAt());
  }
}
