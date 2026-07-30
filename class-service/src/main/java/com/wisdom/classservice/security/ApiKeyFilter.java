package com.wisdom.classservice.security;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class ApiKeyFilter extends OncePerRequestFilter {

    private static final String API_KEY_HEADER = "X-API-KEY";

    @Value("${app.api-key}")
    private String validApiKey;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {

        String path = request.getRequestURI();

        return !path.startsWith("/api/classes");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String receivedApiKey =
                request.getHeader(API_KEY_HEADER);

        if (receivedApiKey == null ||
                !receivedApiKey.equals(validApiKey)) {

            response.setStatus(
                    HttpServletResponse.SC_UNAUTHORIZED
            );

            response.setContentType("application/json");

            response.getWriter().write(
                    "{\"status\":401,"
                    + "\"error\":\"Unauthorized\","
                    + "\"message\":\"Invalid or missing API key\"}"
            );

            return;
        }

        filterChain.doFilter(request, response);
    }
}