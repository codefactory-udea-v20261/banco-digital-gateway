package com.udea.bancodigital.gateway.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class IdentityServiceClient {

    private final WebClient webClient;

    public IdentityServiceClient(
            WebClient.Builder webClientBuilder,
            @Value("${services.identity.url}") String identityServiceUrl
    ) {
        this.webClient = webClientBuilder.baseUrl(identityServiceUrl).build();
    }

    public Mono<TokenValidationResponse> validateToken(String token) {
        return webClient.post()
                .uri("/api/v1/auth/validate-token")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(token)
                .retrieve()
                .bodyToMono(TokenValidationResponse.class)
                .onErrorResume(ex -> {
                    log.warn("Identity token validation failed: {}", ex.getMessage());
                    return Mono.just(TokenValidationResponse.inactive());
                });
    }
}
