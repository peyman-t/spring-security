package com.postgrad.securitydemo.service;

import com.postgrad.securitydemo.model.Resource;
import com.postgrad.securitydemo.repository.ResourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ResourceService {

    private final ResourceRepository resourceRepository;

    /**
     * Get all public resources (no auth required)
     */
    public List<Resource> getPublicResources() {
        return resourceRepository.findByPublicResourceTrue();
    }

    /**
     * Get all resources (requires authentication)
     */
    @PreAuthorize("isAuthenticated()")
    public List<Resource> getAllResources() {
        return resourceRepository.findAll();
    }

    /**
     * Get resources that the current user owns
     */
    @PreAuthorize("isAuthenticated()")
    public List<Resource> getMyResources() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return resourceRepository.findByOwner(auth.getName());
    }

    /**
     * Get resources by ID - only if public OR user is owner OR user has admin role
     */
    @PostAuthorize("returnObject.isPresent() && (returnObject.get().isPublicResource() || " +
            "returnObject.get().getOwner() == authentication.name || " +
            "hasRole('ADMIN'))")
    public Optional<Resource> getResourceById(Long id) {
        return resourceRepository.findById(id);
    }

    /**
     * Create a new resource - automatically set current user as owner
     */
    @PreAuthorize("isAuthenticated()")
    public Resource createResource(Resource resource) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        resource.setOwner(auth.getName());
        return resourceRepository.save(resource);
    }

    /**
     * Update a resource - only if user is owner OR has admin role
     */
    @PreAuthorize("@resourceService.getResourceById(#id).isPresent() && " +
            "(@resourceService.getResourceById(#id).get().getOwner() == authentication.name || " +
            "hasRole('ADMIN'))")
    public Resource updateResource(Long id, Resource resource) {
        resource.setId(id);
        return resourceRepository.save(resource);
    }

    /**
     * Delete a resource - only if user is owner OR has admin role
     */
    @PreAuthorize("@resourceService.getResourceById(#id).isPresent() && " +
            "(@resourceService.getResourceById(#id).get().getOwner() == authentication.name || " +
            "hasRole('ADMIN'))")
    public void deleteResource(Long id) {
        resourceRepository.deleteById(id);
    }

    /**
     * Get resources that require specific role
     */
    @PreAuthorize("hasRole(#role)")
    public List<Resource> getResourcesByRequiredRole(String role) {
        return resourceRepository.findByRequiredRole(role);
    }
}