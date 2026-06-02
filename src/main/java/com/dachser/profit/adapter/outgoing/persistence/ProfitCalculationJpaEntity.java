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
import java.time.LocalDateTime;

/**
 * JPA mapping for the {@code profit_calculation} table: a stored profit/loss result for a shipment.
 *
 * <p>The instant is stored as {@link LocalDateTime} (UTC) to map cleanly onto the SQL {@code
 * TIMESTAMP} column; the persistence adapter converts to/from {@link java.time.Instant} at the
 * domain boundary.
 */
@Entity
@Table(name = "profit_calculation")
class ProfitCalculationJpaEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "shipment_id", nullable = false)
  private ShipmentJpaEntity shipment;

  @Column(name = "total_income", nullable = false, precision = 12, scale = 2)
  private BigDecimal totalIncome;

  @Column(name = "total_cost", nullable = false, precision = 12, scale = 2)
  private BigDecimal totalCost;

  @Column(name = "profit_or_loss", nullable = false, precision = 12, scale = 2)
  private BigDecimal profitOrLoss;

  @Column(name = "calculated_at", nullable = false)
  private LocalDateTime calculatedAt;

  protected ProfitCalculationJpaEntity() {
    // required by JPA
  }

  Long getId() {
    return id;
  }

  ShipmentJpaEntity getShipment() {
    return shipment;
  }

  void setShipment(ShipmentJpaEntity shipment) {
    this.shipment = shipment;
  }

  BigDecimal getTotalIncome() {
    return totalIncome;
  }

  void setTotalIncome(BigDecimal totalIncome) {
    this.totalIncome = totalIncome;
  }

  BigDecimal getTotalCost() {
    return totalCost;
  }

  void setTotalCost(BigDecimal totalCost) {
    this.totalCost = totalCost;
  }

  BigDecimal getProfitOrLoss() {
    return profitOrLoss;
  }

  void setProfitOrLoss(BigDecimal profitOrLoss) {
    this.profitOrLoss = profitOrLoss;
  }

  LocalDateTime getCalculatedAt() {
    return calculatedAt;
  }

  void setCalculatedAt(LocalDateTime calculatedAt) {
    this.calculatedAt = calculatedAt;
  }
}
