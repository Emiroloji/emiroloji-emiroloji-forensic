package com.forensic.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

@Component
public class EncryptionValidationFilter implements GlobalFilter, Ordered {

    // Endpoints that require HTTPS
    private static final List<String> HTTPS_REQUIRED_PATHS = Arrays.asList(
        "/api/auth/",
        "/api/cases/",
        "/api/storage/",
        "/api/audit/"
    );

    // Endpoints that require encryption headers
    private static final List<String> ENCRYPTION_REQUIRED_PATHS = Arrays.asList(
        "/api/storage/upload",
        "/api/storage/download"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();
        
        // Check HTTPS requirement
        if (requiresHttps(path) && !isHttps(request)) {
            return handleSecurityViolation(exchange, "HTTPS required for this endpoint");
        }
        
        // Check encryption headers
        if (requiresEncryptionHeaders(path) && !hasEncryptionHeaders(request)) {
            return handleSecurityViolation(exchange, "Encryption headers required for this endpoint");
        }
        
        return chain.filter(exchange);
    }

    private boolean requiresHttps(String path) {
        return HTTPS_REQUIRED_PATHS.stream().anyMatch(path::startsWith);
    }

    private boolean isHttps(ServerHttpRequest request) {
        String scheme = request.getURI().getScheme();
        String forwardedProto = request.getHeaders().getFirst("X-Forwarded-Proto");
        return "https".equals(scheme) || "https".equals(forwardedProto);
    }

    private boolean requiresEncryptionHeaders(String path) {
        return ENCRYPTION_REQUIRED_PATHS.stream().anyMatch(path::startsWith);
    }

    private boolean hasEncryptionHeaders(ServerHttpRequest request) {
        String encryptionKey = request.getHeaders().getFirst("X-Encryption-Key");
        String encryptionAlgorithm = request.getHeaders().getFirst("X-Encryption-Algorithm");
        String encryptionMode = request.getHeaders().getFirst("X-Encryption-Mode");
        
        return encryptionKey != null && encryptionAlgorithm != null && encryptionMode != null;
    }

    private Mono<Void> handleSecurityViolation(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.BAD_REQUEST);
        
        String body = "{\"error\":\"Security Violation\",\"message\":\"" + message + "\"}";
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -4; // Highest priority
    }
}
