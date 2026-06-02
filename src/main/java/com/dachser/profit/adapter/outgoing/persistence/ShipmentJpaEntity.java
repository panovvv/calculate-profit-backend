package com.dachser.profit.adapter.outgoing.persistence;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA mapping for the {@code shipment} table. Lives in the outbound persistence adapter so the
 * domain model stays free of persistence concerns. Owns its incomes and costs via cascade + orphan
 * removal.
 */
@Entity
@Table(name = "shipment")
class ShipmentJpaEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 50, unique = true)
  private String reference;

  @Column(length = 255)
  private String description;

  @OneToMany(mappedBy = "shipment", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<IncomeJpaEntity> incomes = new ArrayList<>();

  @OneToMany(mappedBy = "shipment", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<CostJpaEntity> costs = new ArrayList<>();

  protected ShipmentJpaEntity() {
    // required by JPA
  }

  /**
   * Replaces all incomes, keeping both sides of the association in sync (orphan removal deletes old
   * rows).
   */
  void replaceIncomes(List<IncomeJpaEntity> newIncomes) {
    incomes.clear();
    newIncomes.forEach(this::addIncome);
  }

  /** Replaces all costs, keeping both sides of the association in sync. */
  void replaceCosts(List<CostJpaEntity> newCosts) {
    costs.clear();
    newCosts.forEach(this::addCost);
  }

  private void addIncome(IncomeJpaEntity income) {
    income.setShipment(this);
    incomes.add(income);
  }

  private void addCost(CostJpaEntity cost) {
    cost.setShipment(this);
    costs.add(cost);
  }

  Long getId() {
    return id;
  }

  String getReference() {
    return reference;
  }

  void setReference(String reference) {
    this.reference = reference;
  }

  String getDescription() {
    return description;
  }

  void setDescription(String description) {
    this.description = description;
  }

  List<IncomeJpaEntity> getIncomes() {
    return incomes;
  }

  List<CostJpaEntity> getCosts() {
    return costs;
  }
}
