package com.dachser.profit.adapter.outgoing.persistence;

import com.dachser.profit.domain.model.CostType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/** JPA mapping for the {@code cost} table (a service cost belonging to a shipment). */
@Entity
@Table(name = "cost")
class CostJpaEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "shipment_id", nullable = false)
  private ShipmentJpaEntity shipment;

  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal amount;

  @Enumerated(EnumType.STRING)
  @Column(name = "cost_type", nullable = false, length = 20)
  private CostType type;

  protected CostJpaEntity() {
    // required by JPA
  }

  CostJpaEntity(BigDecimal amount, CostType type) {
    this.amount = amount;
    this.type = type;
  }

  Long getId() {
    return id;
  }

  BigDecimal getAmount() {
    return amount;
  }

  CostType getType() {
    return type;
  }

  void setShipment(ShipmentJpaEntity shipment) {
    this.shipment = shipment;
  }
}
