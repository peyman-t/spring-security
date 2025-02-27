package com.postgrad.securitydemo.controller;

import com.postgrad.securitydemo.model.Resource;
import com.postgrad.securitydemo.service.ResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicController {

    private final ResourceService resourceService;

    @GetMapping("/resources")
    public ResponseEntity<List<Resource>> getPublicResources() {
        return ResponseEntity.ok(resourceService.getPublicResources());
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "message", "Service is running correctly",
                "version", "1.0"
        ));
    }

    @GetMapping("/info")
    public ResponseEntity<Map<String, String>> appInfo() {
        return ResponseEntity.ok(Map.of(
                "name", "Security Demo Application",
                "description", "Spring Boot application with Keycloak integration",
                "endpoints", "Public and protected endpoints available"
        ));
    }
}
