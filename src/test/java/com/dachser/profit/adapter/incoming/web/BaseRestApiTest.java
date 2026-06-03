package com.dachser.profit.adapter.incoming.web;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

/**
 * Base for REST Assured API tests: boots the app on a random port and exercises the real HTTP stack
 * (security, validation, service, Flyway/H2) — no mocks.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
abstract class BaseRestApiTest {

  @LocalServerPort protected int port;

  /** Request spec carrying the valid dev credentials (HTTP Basic). */
  protected RequestSpecification authenticated() {
    return anonymous().auth().preemptive().basic("dachser", "dachser");
  }

  /** Request spec without credentials. */
  protected RequestSpecification anonymous() {
    return RestAssured.given().baseUri("http://localhost").port(port).contentType(ContentType.JSON);
  }
}
