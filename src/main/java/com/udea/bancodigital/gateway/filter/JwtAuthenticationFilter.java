package com.udea.bancodigital.gateway.filter;

import com.udea.bancodigital.gateway.security.IdentityServiceClient;
import com.udea.bancodigital.gateway.security.TokenValidationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class JwtAuthenticationFilter extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {

    private final IdentityServiceClient identityServiceClient;

    private static final List<String> PUBLIC_ENDPOINTS = Arrays.asList(
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/api/v1/internal/users/provision-client-access",
            "/api/v1/internal/users/exists",
            "/api/v1/health",
            "/swagger-ui",
            "/v3/api-docs",
            "/api-docs",
            "/webjars/swagger-ui"
    );

    public JwtAuthenticationFilter(IdentityServiceClient identityServiceClient) {
        super(Config.class);
        this.identityServiceClient = identityServiceClient;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String path = exchange.getRequest().getURI().getPath();

            if (isPublicEndpoint(path)) {
                return chain.filter(exchange);
            }

            String token = extractToken(exchange);
            if (token == null) {
                return onError(exchange, "Missing Authorization header", HttpStatus.UNAUTHORIZED);
            }

            return identityServiceClient.validateToken(token)
                    .flatMap(validation -> authorize(exchange, chain, validation))
                    .onErrorResume(ex -> {
                        log.error("Token validation error: {}", ex.getMessage(), ex);
                        return onError(exchange, "Token validation failed", HttpStatus.INTERNAL_SERVER_ERROR);
                    });
        };
    }

    private Mono<Void> authorize(
            ServerWebExchange exchange,
            org.springframework.cloud.gateway.filter.GatewayFilterChain chain,
            TokenValidationResponse validation
    ) {
        if (validation == null || !validation.isActive()) {
            return onError(exchange, "Invalid token", HttpStatus.UNAUTHORIZED);
        }

        String userId = validation.getUid() != null ? validation.getUid() : validation.getSub();
        String role = Optional.ofNullable(validation.getAuthorities())
                .orElse(List.of())
                .stream()
                .filter(authority -> authority.startsWith("ROLE_"))
                .findFirst()
                .orElse(null);

        exchange.getAttributes().put("userId", userId);
        exchange.getAttributes().put("role", role);
        exchange.getAttributes().put("subject", validation.getSub());
        exchange.getAttributes().put("authorities", validation.getAuthorities());
        exchange.getAttributes().put("clienteId", validation.getClienteId());

        log.info("Token validated by identity service for user: {}", userId);
        return chain.filter(exchange);
    }

    private String extractToken(ServerWebExchange exchange) {
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    private boolean isPublicEndpoint(String path) {
        return PUBLIC_ENDPOINTS.stream().anyMatch(path::startsWith) || 
               path.endsWith("/api-docs") || 
               path.contains("/swagger-ui/") || 
               path.endsWith("/swagger-ui.html");
    }

    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String body = String.format("{\"error\": \"%s\"}", message);
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    public static class Config {
        // Config properties if needed
    }
}
