package com.dachser.profit.adapter.incoming.web;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Basic security for the API: HTTP Basic authentication over a stateless REST surface.
 *
 * <ul>
 *   <li>CSRF is disabled (no browser session/cookies; clients send credentials per request).
 *   <li>The OpenAPI docs/Swagger UI, the Actuator endpoints (health, metrics, Prometheus, …) and
 *       the dev H2 console are public; everything else requires authentication.
 *   <li>CORS defers to {@link WebCorsConfig} so the configured origins keep working.
 * </ul>
 *
 * Credentials come from {@code spring.security.user.*} (see {@code application.yml}); override them
 * with the {@code API_USER} / {@code API_PASSWORD} environment variables.
 */
@Configuration
@EnableWebSecurity
class SecurityConfig {

  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http.csrf(AbstractHttpConfigurer::disable)
        .cors(Customizer.withDefaults())
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers(
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/v3/api-docs/**",
                        "/actuator/**",
                        "/h2-console/**")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .httpBasic(Customizer.withDefaults())
        // Allow the H2 console (dev tool) to render in a frame on the same origin.
        .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
        .build();
  }
}
