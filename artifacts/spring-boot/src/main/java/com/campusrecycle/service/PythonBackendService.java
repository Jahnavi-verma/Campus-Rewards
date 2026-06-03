package com.campusrecycle.service;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
public class PythonBackendService {

    private final WebClient pythonBackendClient;

    public PythonBackendService(WebClient pythonBackendClient) {
        this.pythonBackendClient = pythonBackendClient;
    }

    public Mono<Map> get(String path) {
        return pythonBackendClient.get()
                .uri(path)
                .retrieve()
                .bodyToMono(Map.class)
                .onErrorResume(WebClientResponseException.class, e ->
                    Mono.error(new RuntimeException("Python backend error: " + e.getMessage())));
    }

    public Mono<Map> post(String path, Object body) {
        return pythonBackendClient.post()
                .uri(path)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .onErrorResume(WebClientResponseException.class, e ->
                    Mono.error(new RuntimeException("Python backend error: " + e.getMessage())));
    }

    public Mono<Map> put(String path, Object body) {
        return pythonBackendClient.put()
                .uri(path)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .onErrorResume(WebClientResponseException.class, e ->
                    Mono.error(new RuntimeException("Python backend error: " + e.getMessage())));
    }

    public Mono<Void> delete(String path) {
        return pythonBackendClient.delete()
                .uri(path)
                .retrieve()
                .bodyToMono(Void.class);
    }
}
