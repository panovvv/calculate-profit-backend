package com.dachser.profit.adapter.incoming.web;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Verifies the security rules end-to-end (REST Assured): the API requires HTTP Basic auth, while
 * the Actuator endpoints and the OpenAPI docs are public.
 */
class AuthApiTest extends BaseRestApiTest {

  public static final String PROFIT_CALCULATIONS_ENDPOINT = "/api/profit/calculations";

  @Test
  void apiRequiresAuthentication() {
    anonymous().get(PROFIT_CALCULATIONS_ENDPOINT).then().statusCode(401);
  }

  @Test
  void apiIsAccessibleWithBasicAuth() {
    authenticated().get(PROFIT_CALCULATIONS_ENDPOINT).then().statusCode(200);
  }

  @Test
  void actuatorEndpointsArePublic() {
    anonymous().get("/actuator").then().statusCode(200);
    anonymous().get("/actuator/health").then().statusCode(200);
    anonymous().get("/actuator/metrics").then().statusCode(200);
  }

  @Test
  void openApiDocsArePublicAndDescribeTheEndpoint() {
    String body = anonymous().get("/v3/api-docs").then().statusCode(200).extract().asString();
    assertThat(body).contains(PROFIT_CALCULATIONS_ENDPOINT);
  }
}
