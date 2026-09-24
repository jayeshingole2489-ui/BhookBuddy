package com.swiftbite.restaurant.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final HeaderBasedSecurityFilter headerBasedSecurityFilter;

    public SecurityConfig(HeaderBasedSecurityFilter headerBasedSecurityFilter) {
        this.headerBasedSecurityFilter = headerBasedSecurityFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .headers(headers ->
                headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
            .authorizeHttpRequests(auth -> auth
                // H2 console
                .requestMatchers("/h2-console/**").permitAll()
                // OpenAPI / Swagger
                .requestMatchers("/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                // Public GET endpoints
                .requestMatchers(HttpMethod.GET, "/api/restaurants/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/menu/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/categories/**").permitAll()
                // Admin endpoints require ROLE_ADMIN
                .requestMatchers("/api/admin/**").hasAuthority("ROLE_ADMIN")
                // Everything else requires authentication
                .anyRequest().authenticated()
            )
            .addFilterBefore(headerBasedSecurityFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
