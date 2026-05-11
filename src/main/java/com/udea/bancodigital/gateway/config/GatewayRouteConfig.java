package com.udea.bancodigital.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRouteConfig {

    @Value("${services.identity.url:http://identity:8080}")
    private String identityUrl;

    @Value("${services.core-banking.url:http://core-banking:8080}")
    private String coreBankingUrl;

    @Value("${services.audit.url:http://audit:8080}")
    private String auditUrl;

    @Value("${services.reporting.url:http://reporting:8080}")
    private String reportingUrl;

    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Documentation Routes (MUST BE FIRST)
                .route("identity-docs", r -> r
                        .path("/api/v1/auth/api-docs")
                        .filters(f -> f.rewritePath("/api/v1/auth/api-docs", "/api-docs"))
                        .uri(identityUrl)
                )
                .route("core-banking-docs", r -> r
                        .path("/api/v1/clientes/api-docs")
                        .filters(f -> f.rewritePath("/api/v1/clientes/api-docs", "/api-docs"))
                        .uri(coreBankingUrl)
                )
                .route("audit-docs", r -> r
                        .path("/api/v1/audit/api-docs")
                        .filters(f -> f.rewritePath("/api/v1/audit/api-docs", "/api-docs"))
                        .uri(auditUrl)
                )
                .route("reporting-docs", r -> r
                        .path("/api/v1/reportes/api-docs")
                        .filters(f -> f.rewritePath("/api/v1/reportes/api-docs", "/api-docs"))
                        .uri(reportingUrl)
                )

                // Identity Service Routes
                .route("identity-service", r -> r
                        .path("/api/v1/auth/**")
                        .uri(identityUrl)
                )
                .route("identity-internal", r -> r
                        .path("/api/v1/internal/users/**")
                        .uri(identityUrl)
                )

                // Core Banking Service Routes
                .route("core-banking-customers", r -> r
                        .path("/api/v1/clientes/**")
                        .uri(coreBankingUrl)
                )
                .route("core-banking-accounts", r -> r
                        .path("/api/v1/cuentas/**")
                        .uri(coreBankingUrl)
                )
                .route("core-banking-transactions", r -> r
                        .path("/api/v1/transacciones/**")
                        .uri(coreBankingUrl)
                )
                .route("core-banking-transfers", r -> r
                        .path("/api/v1/transferencias/**")
                        .uri(coreBankingUrl)
                )

                // Audit Service Routes
                .route("audit-service", r -> r
                        .path("/api/v1/audit/**")
                        .uri(auditUrl)
                )

                // Reporting Service Routes
                .route("reporting-service", r -> r
                        .path("/api/v1/reportes/**")
                        .uri(reportingUrl)
                )

                .build();
    }
}
