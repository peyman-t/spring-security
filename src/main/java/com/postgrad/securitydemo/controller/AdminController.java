package com.postgrad.securitydemo.controller;

import com.postgrad.securitydemo.service.KeycloakUserService;
import lombok.RequiredArgsConstructor;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final KeycloakUserService keycloakUserService;

    @GetMapping("/users/sync")
    public ResponseEntity<Map<String, Object>> synchronizeUsers() {
        List<UserRepresentation> users = keycloakUserService.syncAllUsers();

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Users synchronized successfully",
                "count", users.size()
        ));
    }

    @DeleteMapping("/users/cache")
    public ResponseEntity<Map<String, Object>> clearUserCache() {
        keycloakUserService.clearAllCache();

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "User cache cleared successfully"
        ));
    }

    @DeleteMapping("/users/cache/{userId}")
    public ResponseEntity<Map<String, Object>> clearSpecificUserCache(@PathVariable String userId) {
        keycloakUserService.clearUserCache(userId);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "User cache cleared for user: " + userId
        ));
    }

    @GetMapping("/system/info")
    public ResponseEntity<Map<String, Object>> getSystemInfo() {
        return ResponseEntity.ok(Map.of(
                "uptime", System.currentTimeMillis(),
                "javaVersion", System.getProperty("java.version"),
                "memory", Map.of(
                        "free", Runtime.getRuntime().freeMemory(),
                        "total", Runtime.getRuntime().totalMemory(),
                        "max", Runtime.getRuntime().maxMemory()
                ),
                "processors", Runtime.getRuntime().availableProcessors()
        ));
    }
}
