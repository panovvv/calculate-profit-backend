package com.dachser.profit.adapter.outgoing.persistence;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data repository for {@link ShipmentJpaEntity}. */
interface ShipmentJpaRepository extends JpaRepository<ShipmentJpaEntity, Long> {

  Optional<ShipmentJpaEntity> findByReference(String reference);
}
