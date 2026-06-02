package com.dachser.profit;

import static com.dachser.profit.adapter.incoming.web.SwaggerApiTags.PROFIT;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Spring Boot entry point for the DACHSER "Calculate Profit" backend. */
@SpringBootApplication
@OpenAPIDefinition(
    info =
        @Info(
            title = "Calculate Profit API",
            version = "1.0.0",
            description =
                "Evaluates the income and costs of a shipment's main carriage and calculates its"
                    + " profit or loss (Profit = Total Income - Total Costs).",
            contact = @Contact(name = "DACHSER", url = "https://www.dachser.com"),
            license =
                @License(
                    name = "Apache 2.0",
                    url = "https://www.apache.org/licenses/LICENSE-2.0.html")),
    servers = {@Server(url = "http://localhost:8080", description = "Local development")},
    tags = {@Tag(name = PROFIT, description = "Calculate and retrieve shipment profit/loss")},
    // Apply Basic auth globally so Swagger UI shows an "Authorize" button for the secured
    // endpoints.
    security = {@SecurityRequirement(name = "basicAuth")})
@SecurityScheme(name = "basicAuth", type = SecuritySchemeType.HTTP, scheme = "basic")
public class ProfitApplication {

  public static void main(String[] args) {
    SpringApplication.run(ProfitApplication.class, args);
  }
}
