package com.udea.bancodigital.gateway.filter;

import com.udea.bancodigital.gateway.security.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
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

@Slf4j
@Component
public class JwtAuthenticationFilter extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {

    private final JwtTokenProvider jwtTokenProvider;

    private static final List<String> PUBLIC_ENDPOINTS = Arrays.asList(
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/api/v1/internal/users/provision-client-access",
            "/api/v1/internal/users/exists",
            "/api/v1/health",
            "/swagger-ui.html",
            "/api-docs"
    );

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        super(Config.class);
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String path = exchange.getRequest().getURI().getPath();

            if (isPublicEndpoint(path)) {
                return chain.filter(exchange);
            }

            try {
                String token = extractToken(exchange);
                if (token == null) {
                    return onError(exchange, "Missing Authorization header", HttpStatus.UNAUTHORIZED);
                }

                Claims claims = jwtTokenProvider.validateToken(token);

                if (jwtTokenProvider.isTokenExpired(claims)) {
                    return onError(exchange, "Token expired", HttpStatus.UNAUTHORIZED);
                }

                String userId = jwtTokenProvider.extractSubject(claims);
                String role = jwtTokenProvider.extractRole(claims);

                exchange.getAttributes().put("userId", userId);
                exchange.getAttributes().put("role", role);
                exchange.getAttributes().put("claims", claims);

                log.info("JWT validated for user: {} with role: {}", userId, role);

            } catch (JwtException e) {
                return onError(exchange, "Invalid token: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
            } catch (Exception e) {
                log.error("JWT validation error: {}", e.getMessage());
                return onError(exchange, "Token validation failed", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            return chain.filter(exchange);
        };
    }

    private String extractToken(ServerWebExchange exchange) {
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    private boolean isPublicEndpoint(String path) {
        return PUBLIC_ENDPOINTS.stream().anyMatch(path::startsWith);
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
