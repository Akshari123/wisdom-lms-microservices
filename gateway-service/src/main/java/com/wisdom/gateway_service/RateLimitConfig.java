package com.wisdom.gateway_service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.reactive.ServerHttpRequest;

import reactor.core.publisher.Mono;

@Configuration
public class RateLimitConfig {

    // Identify users by IP address
    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> {
            ServerHttpRequest request = exchange.getRequest();

            String ip = request.getRemoteAddress() != null
                    ? request.getRemoteAddress()
                            .getAddress()
                            .getHostAddress()
                    : "unknown";

            return Mono.just(ip);
        };
    }

    // Custom RateLimiter bean
    @Bean
    public RateLimiter<SimpleRateLimiter.Config> simpleRateLimiter() {
        return new SimpleRateLimiter();
    }

    // Simple in-memory RateLimiter
    public static class SimpleRateLimiter
            implements RateLimiter<SimpleRateLimiter.Config> {

        private final Map<String, AtomicInteger> requests =
                new ConcurrentHashMap<>();

        private final Map<String, Long> timestamps =
                new ConcurrentHashMap<>();

        @Override
        public Mono<Response> isAllowed(String routeId, String id) {

            long now = System.currentTimeMillis();

            timestamps.putIfAbsent(id, now);
            requests.putIfAbsent(id, new AtomicInteger(0));

            long startTime = timestamps.get(id);

            // Reset after 1 second
            if (now - startTime >= 1000) {
                timestamps.put(id, now);
                requests.put(id, new AtomicInteger(0));
            }

            int count = requests.get(id).incrementAndGet();

            // Allow 5 requests per second
            if (count <= 5) {
                return Mono.just(
                        new Response(true, Map.of())
                );
            }

            // Reject after 5 requests
            return Mono.just(
                    new Response(false, Map.of())
            );
        }

        @Override
        public Class<Config> getConfigClass() {
            return Config.class;
        }

        @Override
        public Config newConfig() {
            return new Config();
        }

        @Override
        public Map<String, Config> getConfig() {
            return Map.of();
        }

        public static class Config {
        }
    }
}