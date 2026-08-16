package com.wisdom.studentservice.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ApiKeyFilterTest {

    @Test
    void requestWithoutApiKeyReturnsUnauthorized() throws Exception {
        ApiKeyFilter filter = new ApiKeyFilter("student-secret-2026");
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);
        StringWriter body = new StringWriter();

        when(request.getRequestURI()).thenReturn("/api/students");
        when(request.getHeader("X-API-KEY")).thenReturn(null);
        when(response.getWriter()).thenReturn(new PrintWriter(body));

        filter.doFilter(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(response).setContentType("application/json");
        verifyNoInteractions(filterChain);
        assertEquals("{\"error\":\"Invalid or missing API key\"}", body.toString());
    }

    @Test
    void requestWithValidApiKeyPassesThrough() throws Exception {
        ApiKeyFilter filter = new ApiKeyFilter("student-secret-2026");
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        when(request.getRequestURI()).thenReturn("/api/students");
        when(request.getHeader("X-API-KEY")).thenReturn("student-secret-2026");
        doNothing().when(filterChain).doFilter(request, response);

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }
}
