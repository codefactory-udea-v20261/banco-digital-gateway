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
                        .uri("lb://IDENTITY-SERVICE")
                )
                .route("identity-internal", r -> r
                        .path("/api/v1/internal/users/**")
                        .uri("lb://IDENTITY-SERVICE")
                )

                // Core Banking Service Routes
                .route("core-banking-customers", r -> r
                        .path("/api/v1/clientes/**")
                        .uri("lb://CORE-BANKING-SERVICE")
                )
                .route("core-banking-accounts", r -> r
                        .path("/api/v1/cuentas/**")
                        .uri("lb://CORE-BANKING-SERVICE")
                )
                .route("core-banking-transactions", r -> r
                        .path("/api/v1/transacciones/**")
                        .uri("lb://CORE-BANKING-SERVICE")
                )
                .route("core-banking-transfers", r -> r
                        .path("/api/v1/transferencias/**")
                        .uri("lb://CORE-BANKING-SERVICE")
                )

                // Audit Service Routes
                .route("audit-service", r -> r
                        .path("/api/v1/audit/**")
                        .uri("lb://AUDIT-SERVICE")
                )

                // Reporting Service Routes
                .route("reporting-service", r -> r
                        .path("/api/v1/reportes/**")
                        .uri("lb://REPORTING-SERVICE")
                )

                .build();
    }
}
