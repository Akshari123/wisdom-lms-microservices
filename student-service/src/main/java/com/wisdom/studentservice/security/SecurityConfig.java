package com.wisdom.studentservice.security;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecurityConfig {

    @Bean
    public FilterRegistrationBean<ApiKeyFilter> apiKeyFilterRegistration(
            @Value("${app.api-key}") String validApiKey) {

        FilterRegistrationBean<ApiKeyFilter> registration =
                new FilterRegistrationBean<>();

        registration.setFilter(new ApiKeyFilter(validApiKey));

        registration.addUrlPatterns(
                "/api/students",
                "/api/students/*"
        );

        registration.setOrder(1);

        return registration;
    }
}
