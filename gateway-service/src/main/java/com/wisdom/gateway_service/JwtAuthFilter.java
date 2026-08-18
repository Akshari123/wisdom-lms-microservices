package com.wisdom.gateway_service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.List;

@Component
public class JwtAuthFilter implements GlobalFilter, Ordered {

    @Value("${JWT_SECRET:fallback_secret_must_be_at_least_256_bits_long_for_jjwt_to_work_properly}")
    private String secret;

    private Key key;

    // Open endpoints that bypass JWT validation
    private final List<String> openApiEndpoints = List.of(
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/oauth2/",
            "/api/auth/login/oauth2/",
            "/api/auth/test"
    );

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // 1. Let open API endpoints pass without validation
        boolean isPublic = openApiEndpoints.stream().anyMatch(path::startsWith);
        if (isPublic) {
            return chain.filter(exchange);
        }

        // 2. Protect all /api/ and /payments/ routes
        if (!path.startsWith("/api/") && !path.startsWith("/payments/")) {
            return chain.filter(exchange);
        }

        // 3. Extract and check Authorization header
        String authHeader = request.getHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorizedResponse(exchange, "Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);

        // 4. Parse and validate the JWT signature and expiration
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            if (claims.getExpiration().before(new Date())) {
                return unauthorizedResponse(exchange, "Token has expired");
            }

            // Valid token! Let it proceed to RateLimitConfig and routing.
            return chain.filter(exchange);

        } catch (Exception e) {
            return unauthorizedResponse(exchange, "Invalid token signature or format");
        }
    }

    private Mono<Void> unauthorizedResponse(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String jsonResponse = String.format("{\n  \"status\": 401,\n  \"error\": \"Unauthorized\",\n  \"message\": \"%s\"\n}", message);
        byte[] bytes = jsonResponse.getBytes(StandardCharsets.UTF_8);

        return exchange.getResponse().writeWith(
                Mono.just(exchange.getResponse().bufferFactory().wrap(bytes))
        );
    }

    @Override
    public int getOrder() {
        // Run BEFORE RateLimitConfig (which is -100) to reject unauthorized requests without tracking their rate
        return -200; 
    }
}
