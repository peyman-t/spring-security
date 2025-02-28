package com.postgrad.securitydemo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Security configuration class for the application.
 * This class configures Spring Security to handle JWT-based OAuth2 authentication
 * with Keycloak as the authorization server.
 *
 * The configuration:
 * - Disables CSRF protection for API endpoints
 * - Enables CORS
 * - Sets up stateless session management (no HTTP sessions)
 * - Configures URL-based access control
 * - Sets up JWT authentication with role mapping from Keycloak
 *
 * @Configuration marks this as a Spring configuration class
 * @EnableWebSecurity enables Spring Security's web security features
 * @EnableMethodSecurity enables method-level security using annotations like @PreAuthorize
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    /**
     * Configures the security filter chain for HTTP requests.
     *
     * @param http The HttpSecurity object to configure
     * @return The built SecurityFilterChain
     * @throws Exception If an error occurs during configuration
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF protection since we're using stateless JWT authentication
                .csrf(AbstractHttpConfigurer::disable)
                // Enable CORS with default settings
                .cors(cors -> {})
                // Configure session management to be stateless (no sessions)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                // Configure URL-based access control
                .authorizeHttpRequests(authorize -> authorize
                        // Public endpoints accessible to all
                        .requestMatchers("/api/public/**").permitAll()
                        .requestMatchers("/api/health/**").permitAll()
                        .requestMatchers("/api/docs/**").permitAll()
                        // User-specific endpoints require USER role
                        .requestMatchers("/api/user/**").hasRole("USER")
                        // Admin endpoints require ADMIN role
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        // All other endpoints require authentication
                        .anyRequest().authenticated()
                )
                // Configure OAuth2 resource server with JWT authentication
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                // Use custom JWT authentication converter to extract roles
                                .jwtAuthenticationConverter(jwtAuthenticationConverter())
                        )
                );

        return http.build();
    }

    /**
     * Creates a custom JWT authentication converter that extracts roles from Keycloak tokens.
     * This converter will map Keycloak's role structure to Spring Security authorities.
     *
     * @return A JwtAuthenticationConverter configured with the KeycloakRoleConverter
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(new KeycloakRoleConverter());
        return converter;
    }

    /**
     * Converter class for extracting Keycloak roles from a JWT token and converting them
     * to Spring Security GrantedAuthority objects.
     *
     * This converter handles both realm roles and client-specific roles from Keycloak,
     * prefixing them with "ROLE_" to match Spring Security's role-based authorization model.
     */
    static class KeycloakRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {
        private final JwtGrantedAuthoritiesConverter defaultConverter = new JwtGrantedAuthoritiesConverter();
        private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(KeycloakRoleConverter.class);

        /**
         * Converts JWT claims to a collection of GrantedAuthority objects.
         * Extracts roles from both realm_access and resource_access claims in the Keycloak JWT.
         *
         * @param jwt The JWT token to extract authorities from
         * @return A collection of GrantedAuthority objects representing the user's roles
         */
        @Override
        public Collection<GrantedAuthority> convert(Jwt jwt) {
            // Get default authorities using the standard converter
            Collection<GrantedAuthority> authorities = defaultConverter.convert(jwt);

            // Log JWT claims and default authorities for debugging
            log.debug("JWT claims: {}", jwt.getClaims());
            log.debug("Default authorities: {}", authorities);
            System.out.println("JWT claims: " + jwt.getClaims());
            System.out.println("Default authorities: " + authorities);

            // Extract realm roles from the JWT token's realm_access claim
            Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
            if (realmAccess != null && realmAccess.containsKey("roles")) {
                @SuppressWarnings("unchecked")
                Collection<String> roles = (Collection<String>) realmAccess.get("roles");
                log.debug("Realm roles: {}", roles);
                roles.forEach(role -> {
                    // Convert each role to an authority with the "ROLE_" prefix
                    SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role.toUpperCase());
                    log.debug("Adding realm authority: {}", authority);
                    authorities.add(authority);
                });
            } else {
                log.debug("No realm_access roles found");
            }

            // Extract client-specific roles from the JWT token's resource_access claim
            Map<String, Object> resourceAccess = jwt.getClaimAsMap("resource_access");
            if (resourceAccess != null) {
                log.debug("Resource access: {}", resourceAccess);
                resourceAccess.forEach((clientId, clientAccess) -> {
                    if (clientAccess instanceof Map) {
                        Map<String, Object> clientAccessMap = (Map<String, Object>) clientAccess;
                        if (clientAccessMap.containsKey("roles")) {
                            @SuppressWarnings("unchecked")
                            Collection<String> roles = (Collection<String>) clientAccessMap.get("roles");
                            log.debug("Client '{}' roles: {}", clientId, roles);
                            roles.forEach(role -> {
                                // Convert each client role to an authority with the "ROLE_" prefix
                                SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role.toUpperCase());
                                log.debug("Adding client authority: {}", authority);
                                authorities.add(authority);
                            });
                        }
                    }
                });
            } else {
                log.debug("No resource_access roles found");
            }

            // Log the final set of authorities for debugging
            log.debug("Final authorities: {}", authorities);
            return authorities;
        }
    }
}