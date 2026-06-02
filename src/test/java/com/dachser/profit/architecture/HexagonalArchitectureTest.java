package com.dachser.profit.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * Enforces the hexagonal (ports &amp; adapters) boundaries at build time, so the architecture
 * cannot silently erode: dependencies only ever point inward (adapters -> application -> domain),
 * the domain stays framework-free, and the inbound/outbound adapters never call each other
 * directly.
 */
@AnalyzeClasses(
    packages = "com.dachser.profit",
    importOptions = ImportOption.DoNotIncludeTests.class)
class HexagonalArchitectureTest {

  private static final String DOMAIN = "com.dachser.profit.domain..";
  private static final String APPLICATION = "com.dachser.profit.application..";
  private static final String INBOUND = "com.dachser.profit.adapter.incoming..";
  private static final String OUTBOUND = "com.dachser.profit.adapter.outgoing..";

  private static final String SPRING = "org.springframework..";
  private static final String JPA = "jakarta.persistence..";

  // --- Domain is the pure core: no framework, no outer layers ---------------------------------

  @ArchTest
  static final ArchRule domain_is_framework_free =
      noClasses()
          .that()
          .resideInAPackage(DOMAIN)
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(SPRING, JPA)
          .because("the domain must remain free of Spring and JPA");

  @ArchTest
  static final ArchRule domain_does_not_depend_on_application =
      noClasses()
          .that()
          .resideInAPackage(DOMAIN)
          .should()
          .dependOnClassesThat()
          .resideInAPackage(APPLICATION);

  @ArchTest
  static final ArchRule domain_does_not_depend_on_adapters =
      noClasses()
          .that()
          .resideInAPackage(DOMAIN)
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(INBOUND, OUTBOUND);

  // --- Application depends only on the domain (and its own ports), never on adapters ----------

  @ArchTest
  static final ArchRule application_does_not_depend_on_adapters =
      noClasses()
          .that()
          .resideInAPackage(APPLICATION)
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(INBOUND, OUTBOUND);

  @ArchTest
  static final ArchRule application_does_not_depend_on_jpa =
      noClasses()
          .that()
          .resideInAPackage(APPLICATION)
          .should()
          .dependOnClassesThat()
          .resideInAPackage(JPA)
          .because("persistence details belong to the outbound adapter, not the application core");

  // --- Adapters must not short-circuit the core by calling each other -------------------------

  @ArchTest
  static final ArchRule inbound_does_not_depend_on_outbound =
      noClasses()
          .that()
          .resideInAPackage(INBOUND)
          .should()
          .dependOnClassesThat()
          .resideInAPackage(OUTBOUND)
          .because(
              "inbound adapters must go through the application core, not the outbound adapter");

  @ArchTest
  static final ArchRule outbound_does_not_depend_on_inbound =
      noClasses()
          .that()
          .resideInAPackage(OUTBOUND)
          .should()
          .dependOnClassesThat()
          .resideInAPackage(INBOUND);
}
