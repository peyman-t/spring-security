package com.postgrad.securitydemo.controller;

import com.postgrad.securitydemo.model.Resource;
import com.postgrad.securitydemo.service.KeycloakUserService;
import com.postgrad.securitydemo.service.ResourceService;
import lombok.RequiredArgsConstructor;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final ResourceService resourceService;
    private final KeycloakUserService keycloakUserService;

    @GetMapping("/resources")
    public ResponseEntity<List<Resource>> getUserResources() {
        return ResponseEntity.ok(resourceService.getMyResources());
    }

    @GetMapping("/resources/all")
    public ResponseEntity<List<Resource>> getAllResources() {
        return ResponseEntity.ok(resourceService.getAllResources());
    }

    @GetMapping("/resources/{id}")
    public ResponseEntity<Resource> getResourceById(@PathVariable Long id) {
        Optional<Resource> resource = resourceService.getResourceById(id);
        return resource.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/resources")
    public ResponseEntity<Resource> createResource(@RequestBody Resource resource) {
        return ResponseEntity.ok(resourceService.createResource(resource));
    }

    @PutMapping("/resources/{id}")
    public ResponseEntity<Resource> updateResource(@PathVariable Long id, @RequestBody Resource resource) {
        return ResponseEntity.ok(resourceService.updateResource(id, resource));
    }

    @DeleteMapping("/resources/{id}")
    public ResponseEntity<Void> deleteResource(@PathVariable Long id) {
        resourceService.deleteResource(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> getUserProfile() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // Get user details from Keycloak
        UserRepresentation userRep = keycloakUserService.getUserInfo(auth.getName());

        if (userRep != null) {
            return ResponseEntity.ok(Map.of(
                    "id", userRep.getId(),
                    "username", userRep.getUsername(),
                    "email", userRep.getEmail(),
                    "firstName", userRep.getFirstName(),
                    "lastName", userRep.getLastName(),
                    "enabled", userRep.isEnabled(),
                    "roles", auth.getAuthorities()
            ));
        } else {
            // Fallback if user details not available
            return ResponseEntity.ok(Map.of(
                    "id", auth.getName(),
                    "roles", auth.getAuthorities()
            ));
        }
    }
}
