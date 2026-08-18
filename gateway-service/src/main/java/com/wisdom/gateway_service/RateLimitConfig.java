package com.wisdom.gateway_service;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimitConfig implements GlobalFilter, Ordered {

    private final Map<String, AtomicInteger> requests = new ConcurrentHashMap<>();
    private final Map<String, Long> timestamps = new ConcurrentHashMap<>();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // Apply rate limit only to API routes
        if (!path.startsWith("/api/")) {
            return chain.filter(exchange);
        }

        // Client Identification: Prefer JWT, fallback to IP
        String key = request.getRemoteAddress() != null 
                ? request.getRemoteAddress().getAddress().getHostAddress() 
                : "unknown";
                
        String authHeader = request.getHeaders().getFirst("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            key = authHeader.substring(7); // Use JWT token as identifier
        }

        long now = System.currentTimeMillis();
        timestamps.putIfAbsent(key, now);
        requests.putIfAbsent(key, new AtomicInteger(0));

        long startTime = timestamps.get(key);

        // Reset time window (1 second)
        if (now - startTime >= 1000) {
            timestamps.put(key, now);
            requests.put(key, new AtomicInteger(0));
        }

        int count = requests.get(key).incrementAndGet();

        // 5 requests per second limit
        if (count > 5) {
            exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
            exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
            
            String jsonResponse = "{\n  \"status\": 429,\n  \"error\": \"Too Many Requests\",\n  \"message\": \"Rate limit exceeded\"\n}";
            byte[] bytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
            
            return exchange.getResponse().writeWith(
                    Mono.just(exchange.getResponse().bufferFactory().wrap(bytes))
            );
        }

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -100; // Run early, before routing filters
    }
}