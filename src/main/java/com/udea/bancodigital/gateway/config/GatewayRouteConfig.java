package com.udea.bancodigital.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

@Configuration
public class GatewayRouteConfig {

    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Identity Service Routes
                .route("identity-service", r -> r
                        .path("/api/v1/auth/**")
                        .uri("lb://identity-service")
                )
                .route("identity-internal", r -> r
                        .path("/api/v1/internal/users/**")
                        .uri("lb://identity-service")
                )

                // Core Banking Service Routes
                .route("core-banking-customers", r -> r
                        .path("/api/v1/customers/**")
                        .uri("lb://core-banking-service")
                )
                .route("core-banking-accounts", r -> r
                        .path("/api/v1/accounts/**")
                        .uri("lb://core-banking-service")
                )
                .route("core-banking-transactions", r -> r
                        .path("/api/v1/transactions/**")
                        .uri("lb://core-banking-service")
                )

                // Audit Service Routes
                .route("audit-service", r -> r
                        .path("/api/v1/audit/**")
                        .uri("lb://audit-service")
                )

                // Reporting Service Routes
                .route("reporting-service", r -> r
                        .path("/api/v1/reports/**")
                        .uri("lb://reporting-service")
                )

                // Health check endpoints
                .route("health-check", r -> r
                        .path("/health/**")
                        .and()
                        .method(HttpMethod.GET)
                        .uri("lb://core-banking-service")
                )

                .build();
    }
}
