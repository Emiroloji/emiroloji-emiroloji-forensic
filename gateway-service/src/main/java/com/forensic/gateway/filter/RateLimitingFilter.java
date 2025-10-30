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
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimitingFilter implements GlobalFilter, Ordered {

    private final ConcurrentHashMap<String, RateLimitInfo> rateLimitMap = new ConcurrentHashMap<>();
    private final int maxRequests = 100; // Max requests per window
    private final Duration windowDuration = Duration.ofMinutes(1); // 1 minute window

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String clientId = getClientId(request);
        
        // Check rate limit
        if (isRateLimited(clientId)) {
            return handleRateLimitExceeded(exchange);
        }
        
        return chain.filter(exchange);
    }

    private String getClientId(ServerHttpRequest request) {
        // Try to get client IP from various headers
        String clientId = request.getHeaders().getFirst("X-Forwarded-For");
        if (clientId == null || clientId.isEmpty()) {
            clientId = request.getHeaders().getFirst("X-Real-IP");
        }
        if (clientId == null || clientId.isEmpty()) {
            clientId = request.getRemoteAddress() != null ? 
                request.getRemoteAddress().getAddress().getHostAddress() : "unknown";
        }
        return clientId;
    }

    private boolean isRateLimited(String clientId) {
        Instant now = Instant.now();
        RateLimitInfo rateLimitInfo = rateLimitMap.computeIfAbsent(clientId, 
            k -> new RateLimitInfo(now, new AtomicInteger(0)));
        
        // Reset window if expired
        if (Duration.between(rateLimitInfo.getWindowStart(), now).compareTo(windowDuration) > 0) {
            rateLimitInfo.setWindowStart(now);
            rateLimitInfo.getRequestCount().set(0);
        }
        
        // Check if limit exceeded
        int currentCount = rateLimitInfo.getRequestCount().incrementAndGet();
        return currentCount > maxRequests;
    }

    private Mono<Void> handleRateLimitExceeded(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        response.getHeaders().add("X-RateLimit-Limit", String.valueOf(maxRequests));
        response.getHeaders().add("X-RateLimit-Remaining", "0");
        response.getHeaders().add("X-RateLimit-Reset", String.valueOf(
            Instant.now().plus(windowDuration).getEpochSecond()));
        
        String body = "{\"error\":\"Rate limit exceeded\",\"message\":\"Too many requests\"}";
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -2; // Higher priority than security headers
    }

    private static class RateLimitInfo {
        private Instant windowStart;
        private AtomicInteger requestCount;

        public RateLimitInfo(Instant windowStart, AtomicInteger requestCount) {
            this.windowStart = windowStart;
            this.requestCount = requestCount;
        }

        public Instant getWindowStart() {
            return windowStart;
        }

        public void setWindowStart(Instant windowStart) {
            this.windowStart = windowStart;
        }

        public AtomicInteger getRequestCount() {
            return requestCount;
        }
    }
}
