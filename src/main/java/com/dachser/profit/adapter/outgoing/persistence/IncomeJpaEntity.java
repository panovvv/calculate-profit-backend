package com.dachser.profit.adapter.outgoing.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/** JPA mapping for the {@code income} table (a customer payment belonging to a shipment). */
@Entity
@Table(name = "income")
class IncomeJpaEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "shipment_id", nullable = false)
  private ShipmentJpaEntity shipment;

  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal amount;

  protected IncomeJpaEntity() {
    // required by JPA
  }

  IncomeJpaEntity(BigDecimal amount) {
    this.amount = amount;
  }

  Long getId() {
    return id;
  }

  BigDecimal getAmount() {
    return amount;
  }

  void setShipment(ShipmentJpaEntity shipment) {
    this.shipment = shipment;
  }
}
