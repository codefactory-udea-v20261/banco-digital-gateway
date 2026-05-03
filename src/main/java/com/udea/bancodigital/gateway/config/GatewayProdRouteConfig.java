package com.udea.bancodigital.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("prod")
public class GatewayProdRouteConfig {

    @Value("${IDENTITY_SERVICE_URL:http://localhost:8081}")
    private String identityServiceUrl;

    @Value("${CORE_BANKING_SERVICE_URL:http://localhost:8080}")
    private String coreBankingServiceUrl;

    @Value("${AUDIT_SERVICE_URL:http://localhost:8082}")
    private String auditServiceUrl;

    @Value("${REPORTING_SERVICE_URL:http://localhost:8083}")
    private String reportingServiceUrl;

    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Identity Service Routes
                .route("identity-service", r -> r
                        .path("/api/v1/auth/**")
                        .uri(identityServiceUrl)
                )
                .route("identity-internal", r -> r
                        .path("/api/v1/internal/users/**")
                        .uri(identityServiceUrl)
                )

                // Core Banking Service Routes
                .route("core-banking-customers", r -> r
                        .path("/api/v1/clientes/**")
                        .uri(coreBankingServiceUrl)
                )
                .route("core-banking-accounts", r -> r
                        .path("/api/v1/cuentas/**")
                        .uri(coreBankingServiceUrl)
                )
                .route("core-banking-transactions", r -> r
                        .path("/api/v1/transacciones/**")
                        .uri(coreBankingServiceUrl)
                )
                .route("core-banking-transfers", r -> r
                        .path("/api/v1/transferencias/**")
                        .uri(coreBankingServiceUrl)
                )

                // Audit Service Routes
                .route("audit-service", r -> r
                        .path("/api/v1/audit/**")
                        .uri(auditServiceUrl)
                )

                // Reporting Service Routes
                .route("reporting-service", r -> r
                        .path("/api/v1/reportes/**")
                        .uri(reportingServiceUrl)
                )

                .build();
    }
}
