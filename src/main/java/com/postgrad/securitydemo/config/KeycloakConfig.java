package com.postgrad.securitydemo.config;

import org.keycloak.adapters.springboot.KeycloakSpringBootConfigResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration class for Keycloak integration.
 *
 * This class defines the beans necessary for integrating with Keycloak authentication server.
 * It provides configuration for how the application resolves Keycloak settings and
 * handles HTTP client communication with the Keycloak server.
 *
 * The configuration:
 * - Uses Spring Boot properties for Keycloak configuration instead of keycloak.json
 * - Sets up a custom HTTP client factory for Keycloak compatibility with HttpClient 5
 * - Creates a RestTemplate bean for making authenticated requests to Keycloak
 */
@Configuration
public class KeycloakConfig {

    /**
     * Creates a KeycloakSpringBootConfigResolver bean that instructs the Keycloak adapter
     * to use Spring Boot properties for configuration instead of the default keycloak.json file.
     *
     * This allows Keycloak configuration to be defined in application.properties or
     * application.yml, and enables different configurations per environment using Spring profiles.
     *
     * @return A new instance of KeycloakSpringBootConfigResolver
     */
    @Bean
    public KeycloakSpringBootConfigResolver keycloakConfigResolver() {
        return new KeycloakSpringBootConfigResolver();
    }

    /**
     * Creates a custom Keycloak client request factory for HTTP communication.
     *
     * This factory handles compatibility with HttpClient 5 and manages the details of
     * creating properly configured HTTP clients for Keycloak communication.
     * It may include features like connection pooling, timeout configuration,
     * and proper header management for OAuth2 communication.
     *
     * @return A new instance of CustomKeycloakClientRequestFactory
     */
    @Bean
    public CustomKeycloakClientRequestFactory customKeycloakClientRequestFactory() {
        return new CustomKeycloakClientRequestFactory();
    }

    /**
     * Creates a RestTemplate configured for making authenticated requests to Keycloak.
     *
     * This RestTemplate is set up with the custom client factory to ensure proper
     * authentication handling, token management, and other Keycloak-specific requirements.
     * It can be injected in services that need to communicate with the Keycloak server
     * for administrative tasks or token introspection.
     *
     * @param factory The CustomKeycloakClientRequestFactory to use for creating the RestTemplate
     * @return A RestTemplate configured for Keycloak communication
     */
    @Bean
    public RestTemplate keycloakRestTemplate(CustomKeycloakClientRequestFactory factory) {
        return factory.createRestTemplate();
    }
}