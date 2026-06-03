package com.dachser.profit.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * Enforces the hexagonal (ports &amp; adapters) boundaries at build time, so the architecture
 * cannot silently erode: dependencies only ever point inward (adapters -> application -> domain),
 * the domain stays framework-free, and the incoming/outgoing adapters never call each other
 * directly.
 */
@AnalyzeClasses(
    packages = "com.dachser.profit",
    importOptions = ImportOption.DoNotIncludeTests.class)
class HexagonalArchitectureTest {

  private static final String DOMAIN = "com.dachser.profit.domain..";
  private static final String APPLICATION = "com.dachser.profit.application..";
  private static final String INCOMING = "com.dachser.profit.adapter.incoming..";
  private static final String OUTGOING = "com.dachser.profit.adapter.outgoing..";

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
          .resideInAnyPackage(INCOMING, OUTGOING);

  // --- Application depends only on the domain (and its own ports), never on adapters ----------

  @ArchTest
  static final ArchRule application_does_not_depend_on_adapters =
      noClasses()
          .that()
          .resideInAPackage(APPLICATION)
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(INCOMING, OUTGOING);

  @ArchTest
  static final ArchRule application_does_not_depend_on_jpa =
      noClasses()
          .that()
          .resideInAPackage(APPLICATION)
          .should()
          .dependOnClassesThat()
          .resideInAPackage(JPA)
          .because("persistence details belong to the outgoing adapter, not the application core");

  // --- Adapters must not short-circuit the core by calling each other -------------------------

  @ArchTest
  static final ArchRule incoming_does_not_depend_on_outgoing =
      noClasses()
          .that()
          .resideInAPackage(INCOMING)
          .should()
          .dependOnClassesThat()
          .resideInAPackage(OUTGOING)
          .because(
              "incoming adapters must go through the application core, not the outgoing adapter");

  @ArchTest
  static final ArchRule outgoing_does_not_depend_on_incoming =
      noClasses()
          .that()
          .resideInAPackage(OUTGOING)
          .should()
          .dependOnClassesThat()
          .resideInAPackage(INCOMING);
}
