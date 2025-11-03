package com.forensic.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

/**
 * API Gateway Service for Forensic Face Matching System
 * 
 * This service acts as the entry point for all client requests and provides:
 * - Request routing to appropriate microservices
 * - Authentication and authorization
 * - Rate limiting
 * - Request/response logging
 * - Security headers
 */
@SpringBootApplication
public class GatewayServiceApplication {

        public static void main(String[] args) {
                SpringApplication.run(GatewayServiceApplication.class, args);
        }

        @Bean
        public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
                return builder.routes()
                                // Auth Service routes
                                .route("auth-service", r -> r.path("/api/auth/**")
                                                .uri("lb://auth-service"))

                                // Case Management Service routes
                                .route("case-service", r -> r.path("/api/cases/**")
                                                .uri("lb://case-service"))

                                // Storage Service routes
                                .route("storage-service", r -> r.path("/api/storage/**")
                                                .uri("lb://storage-service"))

                                // Audit Service routes
                                .route("audit-service", r -> r.path("/api/audit/**")
                                                .uri("lb://audit-service"))

                                // AI Service routes (Python FastAPI)
                                .route("ai-service", r -> r.path("/api/ai/**")
                                                .uri("http://ai-service:8000"))

                                .build();
        }

}
