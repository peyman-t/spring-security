package com.postgrad.securitydemo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for synchronizing user information from Keycloak
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class KeycloakUserService {

    private final RestTemplate restTemplate;

    // Cache of user information
    private final Map<String, UserRepresentation> userCache = new ConcurrentHashMap<>();

    @Value("${keycloak.auth-server-url}")
    private String keycloakServerUrl;

    @Value("${keycloak.realm}")
    private String realm;

    // Add admin credentials
    @Value("${keycloak.admin.username:admin}")
    private String adminUsername;

    @Value("${keycloak.admin.password:admin}")
    private String adminPassword;

    @Value("${keycloak.admin.client-id:admin-cli}")
    private String adminClientId;

    /**
     * Get admin token from Keycloak
     */
    private String getAdminToken() {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.add("grant_type", "password");
            map.add("client_id", adminClientId);
            map.add("username", adminUsername);
            map.add("password", adminPassword);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    keycloakServerUrl + "/realms/master/protocol/openid-connect/token",
                    request,
                    Map.class
            );

            if (response.getBody() != null && response.getBody().containsKey("access_token")) {
                return (String) response.getBody().get("access_token");
            } else {
                log.error("Failed to get admin token: No access_token in response");
                return null;
            }
        } catch (Exception e) {
            log.error("Error getting admin token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Create HTTP headers with admin token
     */
    private HttpHeaders createAuthHeaders() {
        String token = getAdminToken();
        if (token == null) {
            return null;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return headers;
    }

    /**
     * Get user information from Keycloak - uses cache if available
     */
    public UserRepresentation getUserInfo(String userId) {
        if (userCache.containsKey(userId)) {
            return userCache.get(userId);
        }

        UserRepresentation user = fetchUserFromKeycloak(userId);
        if (user != null) {
            userCache.put(userId, user);
        }
        return user;
    }

    /**
     * Fetch user from Keycloak API
     */
    private UserRepresentation fetchUserFromKeycloak(String userId) {
        try {
            HttpHeaders headers = createAuthHeaders();
            if (headers == null) {
                return null;
            }

            HttpEntity<String> entity = new HttpEntity<>(headers);
            String url = String.format("%s/admin/realms/%s/users/%s", keycloakServerUrl, realm, userId);

            ResponseEntity<UserRepresentation> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    UserRepresentation.class
            );

            return response.getBody();
        } catch (Exception e) {
            log.error("Error fetching user from Keycloak: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Synchronize all users from Keycloak
     */
    public List<UserRepresentation> syncAllUsers() {
        try {
            HttpHeaders headers = createAuthHeaders();
            if (headers == null) {
                return List.of();
            }

            HttpEntity<String> entity = new HttpEntity<>(headers);
            String url = String.format("%s/admin/realms/%s/users", keycloakServerUrl, realm);

            ResponseEntity<List<UserRepresentation>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<List<UserRepresentation>>() {}
            );

            List<UserRepresentation> users = response.getBody();
            if (users != null) {
                users.forEach(user -> userCache.put(user.getId(), user));
                log.info("Synchronized {} users from Keycloak", users.size());
            }

            return users != null ? users : List.of();
        } catch (Exception e) {
            log.error("Error synchronizing users from Keycloak: {}", e.getMessage());
            return List.of();
        }
    }

    /**
     * Scheduled task to sync users every hour
     */
    @Scheduled(fixedRate = 3600000) // Every hour
    public void scheduledUserSync() {
        log.info("Running scheduled user synchronization");
        syncAllUsers();
    }

    /**
     * Clear user from cache to force a refresh
     */
    public void clearUserCache(String userId) {
        userCache.remove(userId);
    }

    /**
     * Clear entire cache
     */
    public void clearAllCache() {
        userCache.clear();
    }
}