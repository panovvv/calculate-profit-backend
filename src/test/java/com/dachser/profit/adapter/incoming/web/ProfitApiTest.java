package com.dachser.profit.adapter.incoming.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

import com.dachser.profit.testsupport.ProfitTestData;
import io.restassured.response.Response;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * End-to-end API tests through the real running server (REST Assured). Each test uses a unique
 * shipment reference, so they are isolated and parallel-safe without cleanup.
 */
class ProfitApiTest extends BaseRestApiTest {

  private Response calculate(String reference, Number income, Number cost, Number additionalCost) {
    return authenticated()
        .body(
            Map.of(
                "shipmentReference", reference,
                "income", income,
                "cost", cost,
                "additionalCost", additionalCost))
        .post("/api/profit/calculations");
  }

  @Test
  void calculatesAndStoresAProfit() {
    String reference = ProfitTestData.uniqueReference();

    Response response = calculate(reference, 1000, 200, 0);

    response.then().statusCode(201);
    assertThat(response.jsonPath().getString("shipmentReference")).isEqualTo(reference);
    assertThat(response.jsonPath().getDouble("income")).isEqualTo(1000.0);
    assertThat(response.jsonPath().getDouble("totalCosts")).isEqualTo(200.0);
    assertThat(response.jsonPath().getDouble("profitOrLoss")).isEqualTo(800.0);
    assertThat(response.jsonPath().getBoolean("profit")).isTrue();
  }

  @Test
  void calculatesAndStoresALoss() {
    String reference = ProfitTestData.uniqueReference();

    Response response = calculate(reference, 500, 700, 200);

    response.then().statusCode(201);
    assertThat(response.jsonPath().getDouble("profitOrLoss")).isEqualTo(-400.0);
    assertThat(response.jsonPath().getBoolean("profit")).isFalse();
  }

  @Test
  void listReturnsAPageFilteredByShipmentReference() {
    String reference = ProfitTestData.uniqueReference();
    calculate(reference, 300, 100, 0).then().statusCode(201);

    Response list = authenticated().get("/api/profit/calculations?shipment=" + reference);

    list.then().statusCode(200).body("page.totalElements", equalTo(1));
    assertThat(list.jsonPath().getList("content.shipmentReference")).containsExactly(reference);
    assertThat(list.jsonPath().getList("content.profitOrLoss", Double.class))
        .containsExactly(200.0);
  }

  @Test
  void pagesResults() {
    String reference = ProfitTestData.uniqueReference();
    calculate(reference, 100, 10, 0).then().statusCode(201);
    calculate(reference, 200, 20, 0).then().statusCode(201);

    authenticated()
        .get("/api/profit/calculations?shipment=" + reference + "&page=0&size=1")
        .then()
        .statusCode(200)
        .body("content.size()", equalTo(1))
        .body("page.size", equalTo(1))
        .body("page.number", equalTo(0))
        .body("page.totalElements", equalTo(2))
        .body("page.totalPages", equalTo(2));
  }

  @Test
  void rejectsInvalidRequestWith400() {
    authenticated()
        .body(
            Map.of("shipmentReference", ProfitTestData.uniqueReference(), "income", -5, "cost", 10))
        .post("/api/profit/calculations")
        .then()
        .statusCode(400)
        .body("detail", not(emptyOrNullString()));
  }

  @Test
  void unknownResourceReturns404() {
    authenticated().get("/api/profit/does-not-exist").then().statusCode(404);
  }
}
