package com.dachser.profit.adapter.outgoing.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data repository for {@link ProfitCalculationJpaEntity}. */
interface ProfitCalculationJpaRepository extends JpaRepository<ProfitCalculationJpaEntity, Long> {

  List<ProfitCalculationJpaEntity> findAllByOrderByCalculatedAtDesc();

  List<ProfitCalculationJpaEntity> findByShipmentReferenceOrderByCalculatedAtDesc(String reference);
}
