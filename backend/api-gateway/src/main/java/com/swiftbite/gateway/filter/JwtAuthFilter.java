package com.swiftbite.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Global JWT authentication filter for the SwiftBite API Gateway.
 *
 * <p>Intercepts all incoming requests, skips public paths, validates the JWT
 * from the Authorization header, and forwards user identity headers to
 * downstream services.</p>
 */
@Component
public class JwtAuthFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String HEADER_USER_EMAIL = "X-User-Email";
    private static final String HEADER_USER_ROLES = "X-User-Roles";

    /**
     * Paths that are always public — no JWT required regardless of method.
     */
    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/auth/**",
            "/api/categories/**"
    );

    /**
     * Paths that are public only for GET (read-only) requests.
     */
    private static final List<String> PUBLIC_GET_PATHS = List.of(
            "/api/restaurants/**",
            "/api/menu/**"
    );

    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final SecretKey signingKey;

    public JwtAuthFilter(@Value("${jwt.secret}") String jwtSecret) {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    @Override
    public int getOrder() {
        return -1;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        HttpMethod method = request.getMethod();

        log.debug("Processing request: {} {}", method, path);

        if (isPublicPath(path, method)) {
            log.debug("Path {} {} is public — skipping JWT validation", method, path);
            return chain.filter(exchange);
        }

        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            log.warn("Missing or malformed Authorization header for {} {}", method, path);
            return sendUnauthorized(exchange, "Missing or malformed Authorization header");
        }

        String token = authHeader.substring(BEARER_PREFIX.length()).trim();

        try {
            Claims claims = parseToken(token);

            String email = claims.getSubject();
            Object userIdObj = claims.get("userId");
            String userId = userIdObj != null ? userIdObj.toString() : "";
            Object rolesObj = claims.get("roles");
            String roles = rolesObj != null ? rolesObj.toString() : "";

            log.debug("JWT valid for user {} — forwarding to downstream", userId);

            ServerHttpRequest mutatedRequest = request.mutate()
                    .header(HEADER_USER_ID, userId != null ? userId : "")
                    .header(HEADER_USER_EMAIL, email != null ? email : "")
                    .header(HEADER_USER_ROLES, roles)
                    .build();

            return chain.filter(exchange.mutate().request(mutatedRequest).build());

        } catch (ExpiredJwtException ex) {
            log.warn("Expired JWT token for {} {}: {}", method, path, ex.getMessage());
            return sendUnauthorized(exchange, "Token has expired");
        } catch (JwtException | IllegalArgumentException ex) {
            log.warn("Invalid JWT token for {} {}: {}", method, path, ex.getMessage());
            return sendUnauthorized(exchange, "Invalid token");
        }
    }

    /**
     * Parses and validates the JWT, returning its claims on success.
     *
     * @param token the raw JWT string (without "Bearer " prefix)
     * @return validated {@link Claims}
     * @throws JwtException if the token is invalid or expired
     */
    private Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Returns true when the request does not require a JWT.
     *
     * <p>Rules:
     * <ul>
     *   <li>Paths in {@code PUBLIC_PATHS} are always allowed.</li>
     *   <li>Paths in {@code PUBLIC_GET_PATHS} are allowed only for GET requests.</li>
     * </ul>
     * </p>
     */
    private boolean isPublicPath(String path, HttpMethod method) {
        for (String pattern : PUBLIC_PATHS) {
            if (pathMatcher.match(pattern, path)) {
                return true;
            }
        }

        if (HttpMethod.GET.equals(method)) {
            for (String pattern : PUBLIC_GET_PATHS) {
                if (pathMatcher.match(pattern, path)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Writes a JSON 401 response and completes the exchange without forwarding downstream.
     */
    private Mono<Void> sendUnauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String body = String.format(
                "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"%s\"}",
                message.replace("\"", "\\\"")
        );

        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }
}
