package com.campusrecycle.controller;

import com.campusrecycle.service.PythonBackendService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/python")
public class PythonProxyController {

    private final PythonBackendService pythonBackendService;

    public PythonProxyController(PythonBackendService pythonBackendService) {
        this.pythonBackendService = pythonBackendService;
    }

    @GetMapping("/{path}")
    public Mono<ResponseEntity<Map>> proxyGet(@PathVariable String path,
                                               Authentication authentication) {
        return pythonBackendService.get("/" + path)
                .map(ResponseEntity::ok)
                .onErrorReturn(ResponseEntity.status(502).build());
    }

    @PostMapping("/{path}")
    public Mono<ResponseEntity<Map>> proxyPost(@PathVariable String path,
                                                @RequestBody(required = false) Map<String, Object> body,
                                                Authentication authentication) {
        Object payload = body != null ? body : Map.of();
        return pythonBackendService.post("/" + path, payload)
                .map(ResponseEntity::ok)
                .onErrorReturn(ResponseEntity.status(502).build());
    }

    @PutMapping("/{path}")
    public Mono<ResponseEntity<Map>> proxyPut(@PathVariable String path,
                                               @RequestBody(required = false) Map<String, Object> body,
                                               Authentication authentication) {
        Object payload = body != null ? body : Map.of();
        return pythonBackendService.put("/" + path, payload)
                .map(ResponseEntity::ok)
                .onErrorReturn(ResponseEntity.status(502).build());
    }
}
