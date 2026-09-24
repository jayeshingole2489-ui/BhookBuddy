package com.swiftbite.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * CORS configuration for the SwiftBite API Gateway.
 *
 * <p>Allows the React frontend (http://localhost:3000) to make cross-origin
 * requests to the gateway and exposes the Authorization header so that
 * the browser can read it from responses.</p>
 *
 * <p>Note: Spring Cloud Gateway also has built-in {@code globalcors} YAML
 * support; this programmatic bean ensures CORS is applied before gateway
 * filters run, which avoids 401 responses being rejected by the browser
 * as opaque errors before preflight completes.</p>
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // Allow the React development server origin
        config.setAllowedOrigins(List.of("http://localhost:3000"));

        // Allow all standard HTTP methods including preflight
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD"));

        // Allow all request headers (the browser will send its own list in the preflight)
        config.setAllowedHeaders(List.of("*"));

        // Allow cookies and Authorization header to be sent cross-origin
        config.setAllowCredentials(true);

        // Expose Authorization so the browser JavaScript can read it from responses
        config.setExposedHeaders(List.of("Authorization"));

        // Cache preflight result for 1 hour to reduce OPTIONS round-trips
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsWebFilter(source);
    }
}
