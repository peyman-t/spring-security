package com.postgrad.securitydemo.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.keycloak.adapters.springsecurity.client.KeycloakRestTemplate;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Slf4j
public class CustomKeycloakClientRequestFactory {

    private final HttpClient httpClient;

    public CustomKeycloakClientRequestFactory() {
        this.httpClient = HttpClientBuilder.create().build();
    }

    public RestTemplate createRestTemplate() {
        HttpComponentsClientHttpRequestFactory requestFactory =
                new HttpComponentsClientHttpRequestFactory();
        requestFactory.setHttpClient(httpClient);

        log.debug("Created custom Keycloak RestTemplate with HttpClient 5");
        return new RestTemplate(requestFactory);
    }
}