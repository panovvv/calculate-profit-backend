package com.dachser.profit.adapter.outgoing.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data repository for {@link ProfitCalculationJpaEntity}. */
interface ProfitCalculationJpaRepository extends JpaRepository<ProfitCalculationJpaEntity, Long> {

  // findAll(Pageable) is inherited from JpaRepository; sort order comes from the Pageable.
  Page<ProfitCalculationJpaEntity> findByShipmentReference(
      String shipmentReference, Pageable pageable);
}
