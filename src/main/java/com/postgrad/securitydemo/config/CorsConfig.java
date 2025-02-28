package com.postgrad.securitydemo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;

/**
 * Configuration class for Cross-Origin Resource Sharing (CORS) settings.
 *
 * CORS is a security feature implemented by browsers that restricts web page requests
 * to another domain outside the domain from which the first resource was served.
 * This class defines the CORS policy for the application, allowing specific origins
 * to access the API endpoints.
 *
 * The configuration specifies:
 * - Allowed origins (domains that can access the API)
 * - Allowed HTTP methods
 * - Allowed headers in requests
 * - Exposed headers in responses
 * - Whether credentials are allowed
 * - How long the browser should cache the CORS configuration
 */
@Configuration
public class CorsConfig {

    /**
     * Creates and configures a CorsFilter bean to handle Cross-Origin Resource Sharing.
     *
     * This filter intercepts incoming HTTP requests and applies the CORS policy
     * by adding appropriate headers to responses. It allows the API to be accessed
     * from specified frontend applications running on different domains.
     *
     * @return A configured CorsFilter instance that implements the defined CORS policy
     */
    @Bean
    public CorsFilter corsFilter() {
        // Create a new CORS configuration object
        CorsConfiguration corsConfig = new CorsConfiguration();

        // Allow cookies and authentication headers to be sent with CORS requests
        corsConfig.setAllowCredentials(true);

        // Define the specific origins (domains) that are allowed to access the API
        // In this case, the local development server and a production domain
        corsConfig.setAllowedOrigins(Arrays.asList("http://localhost:3000", "https://example.com"));

        // Specify which HTTP methods are allowed in CORS requests
        corsConfig.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));

        // Define which headers can be included in requests from the client
        corsConfig.setAllowedHeaders(Arrays.asList("Origin", "Content-Type", "Accept", "Authorization"));

        // Specify which response headers should be exposed to the client application
        corsConfig.setExposedHeaders(Arrays.asList("X-Auth-Token"));

        // Set how long (in seconds) the browser should cache the CORS configuration
        // 3600 seconds = 1 hour
        corsConfig.setMaxAge(3600L);

        // Create a URL-based CORS configuration source
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        // Apply the CORS configuration to all paths in the application ("/**")
        source.registerCorsConfiguration("/**", corsConfig);

        // Create and return the CORS filter with the defined configuration
        return new CorsFilter(source);
    }
}