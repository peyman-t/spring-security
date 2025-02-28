package com.postgrad.securitydemo.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.keycloak.adapters.springsecurity.client.KeycloakRestTemplate;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * Factory class for creating HTTP clients that are compatible with Keycloak authentication.
 *
 * This custom implementation addresses compatibility issues between Keycloak and HttpClient 5.
 * The standard KeycloakRestTemplate uses older HttpClient versions, which can cause conflicts
 * when the application needs to use HttpClient 5 for other purposes.
 *
 * This factory:
 * - Creates an HttpClient 5 instance
 * - Configures a compatible request factory
 * - Produces a RestTemplate that can be used for authenticated Keycloak communication
 *
 * @Slf4j is a Lombok annotation that automatically creates a logger field
 */
@Slf4j
public class CustomKeycloakClientRequestFactory {

    /**
     * The HttpClient instance used for all HTTP communication with Keycloak.
     * Using a single instance allows for connection pooling and efficient resource usage.
     */
    private final HttpClient httpClient;

    /**
     * Constructor that initializes a new HttpClient with default settings.
     *
     * This creates a basic HttpClient instance using HttpClient 5's builder pattern.
     * For production use, this client could be further configured with:
     * - Connection timeouts
     * - Connection pooling settings
     * - Retry strategies
     * - SSL configuration
     */
    public CustomKeycloakClientRequestFactory() {
        this.httpClient = HttpClientBuilder.create().build();
    }

    /**
     * Creates and configures a RestTemplate that is compatible with Keycloak.
     *
     * This method:
     * 1. Creates an HttpComponentsClientHttpRequestFactory using the HttpClient 5 instance
     * 2. Configures the request factory with the HttpClient
     * 3. Creates a RestTemplate with the configured factory
     *
     * Note: This returns a standard RestTemplate rather than KeycloakRestTemplate,
     * so additional authentication handling may be needed depending on the use case.
     *
     * @return A RestTemplate configured for Keycloak API communication
     */
    public RestTemplate createRestTemplate() {
        // Create a request factory that bridges Spring's HTTP abstraction with HttpClient 5
        HttpComponentsClientHttpRequestFactory requestFactory =
                new HttpComponentsClientHttpRequestFactory();

        // Configure the factory to use our HttpClient instance
        requestFactory.setHttpClient(httpClient);

        // Log creation for debugging purposes
        log.debug("Created custom Keycloak RestTemplate with HttpClient 5");

        // Return a new RestTemplate with our custom factory
        return new RestTemplate(requestFactory);
    }
}