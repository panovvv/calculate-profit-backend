package com.dachser.profit.adapter.incoming.web;

import static org.assertj.core.api.Assertions.assertThat;

import com.dachser.profit.testsupport.ProfitTestData;
import io.restassured.response.Response;
import java.util.List;
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
  void listFiltersByShipmentReference() {
    String reference = ProfitTestData.uniqueReference();
    calculate(reference, 300, 100, 0).then().statusCode(201);

    Response list = authenticated().get("/api/profit/calculations?shipment=" + reference);

    list.then().statusCode(200);
    List<String> references = list.jsonPath().getList("shipmentReference");
    assertThat(references).containsExactly(reference);
    assertThat(list.jsonPath().getList("profitOrLoss", Double.class)).containsExactly(200.0);
  }

  @Test
  void rejectsInvalidRequestWith400() {
    authenticated()
        .body(
            Map.of("shipmentReference", ProfitTestData.uniqueReference(), "income", -5, "cost", 10))
        .post("/api/profit/calculations")
        .then()
        .statusCode(400)
        .body("detail", org.hamcrest.Matchers.not(org.hamcrest.Matchers.emptyOrNullString()));
  }

  @Test
  void unknownResourceReturns404() {
    authenticated().get("/api/profit/does-not-exist").then().statusCode(404);
  }
}
