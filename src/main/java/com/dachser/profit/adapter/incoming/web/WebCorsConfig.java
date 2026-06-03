package com.dachser.profit.adapter.incoming.web;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * CORS for the API, exposed as a {@link CorsConfigurationSource} bean so Spring Security's filter
 * chain handles the preflight {@code OPTIONS} (before authentication) — an MVC-level CORS config
 * would be bypassed because the security filters run first.
 *
 * <p>Allowed origins come from {@code app.cors.allowed-origins} (env var {@code
 * CORS_ALLOWED_ORIGINS}, comma-separated), defaulting to the local Angular dev server. Values may
 * be exact origins or patterns (e.g. {@code https://*.vercel.app}).
 */
@Configuration
class WebCorsConfig {

  private final List<String> allowedOriginPatterns;

  WebCorsConfig(@Value("${app.cors.allowed-origins}") List<String> allowedOriginPatterns) {
    this.allowedOriginPatterns = allowedOriginPatterns;
  }

  @Bean
  CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOriginPatterns(allowedOriginPatterns);
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(List.of("*"));
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/api/**", config);
    return source;
  }
}
