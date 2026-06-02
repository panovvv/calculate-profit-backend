package com.dachser.profit.adapter.incoming.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

/**
 * Verifies the security rules end-to-end against a real embedded server: the API requires HTTP
 * Basic auth, while the health endpoint and the OpenAPI docs are public. (The docs check also
 * confirms the generated spec contains our endpoint.)
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SecurityIntegrationTest {

  @LocalServerPort private int port;

  private final HttpClient client = HttpClient.newHttpClient();

  @Test
  void apiRequiresAuthentication() throws Exception {
    assertThat(get("/api/profit/calculations", null).statusCode()).isEqualTo(401);
  }

  @Test
  void apiAccessibleWithBasicAuth() throws Exception {
    assertThat(get("/api/profit/calculations", basic("dachser", "dachser")).statusCode())
        .isEqualTo(200);
  }

  @Test
  void healthEndpointIsPublic() throws Exception {
    assertThat(get("/actuator/health", null).statusCode()).isEqualTo(200);
  }

  @Test
  void otherActuatorEndpointsArePublic() throws Exception {
    assertThat(get("/actuator/metrics", null).statusCode()).isEqualTo(200);
  }

  @Test
  void openApiDocsArePublicAndDescribeTheEndpoint() throws Exception {
    HttpResponse<String> response = get("/v3/api-docs", null);
    assertThat(response.statusCode()).isEqualTo(200);
    assertThat(response.body()).contains("/api/profit/calculations");
  }

  private HttpResponse<String> get(String path, String authHeader) throws Exception {
    HttpRequest.Builder request =
        HttpRequest.newBuilder(URI.create("http://localhost:" + port + path)).GET();
    if (authHeader != null) {
      request.header("Authorization", authHeader);
    }
    return client.send(request.build(), HttpResponse.BodyHandlers.ofString());
  }

  private static String basic(String user, String password) {
    String token =
        Base64.getEncoder()
            .encodeToString((user + ":" + password).getBytes(StandardCharsets.UTF_8));
    return "Basic " + token;
  }
}
