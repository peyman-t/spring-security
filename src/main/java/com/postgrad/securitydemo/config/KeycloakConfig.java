package com.postgrad.securitydemo.config;

import org.keycloak.adapters.springboot.KeycloakSpringBootConfigResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class KeycloakConfig {

    /**
     * Use Spring Boot properties instead of default keycloak.json
     */
    @Bean
    public KeycloakSpringBootConfigResolver keycloakConfigResolver() {
        return new KeycloakSpringBootConfigResolver();
    }

    /**
     * Custom client factory for HttpClient 5 compatibility
     */
    @Bean
    public CustomKeycloakClientRequestFactory customKeycloakClientRequestFactory() {
        return new CustomKeycloakClientRequestFactory();
    }

    /**
     * Client for making authenticated requests to Keycloak
     */
    @Bean
    public RestTemplate keycloakRestTemplate(CustomKeycloakClientRequestFactory factory) {
        return factory.createRestTemplate();
    }
}