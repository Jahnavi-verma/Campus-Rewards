package com.campusrecycle.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${app.python-backend-url:http://localhost:8000}")
    private String pythonBackendUrl;

    @Bean
    public WebClient pythonBackendClient() {
        return WebClient.builder()
                .baseUrl(pythonBackendUrl)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}
