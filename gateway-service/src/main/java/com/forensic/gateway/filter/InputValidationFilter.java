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
import java.util.regex.Pattern;

@Component
public class InputValidationFilter implements GlobalFilter, Ordered {

    // SQL injection patterns
    private static final List<Pattern> SQL_INJECTION_PATTERNS = Arrays.asList(
        Pattern.compile("(?i).*union.*select.*", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(?i).*drop.*table.*", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(?i).*insert.*into.*", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(?i).*delete.*from.*", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(?i).*update.*set.*", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(?i).*or.*1=1.*", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(?i).*and.*1=1.*", Pattern.CASE_INSENSITIVE)
    );

    // XSS patterns
    private static final List<Pattern> XSS_PATTERNS = Arrays.asList(
        Pattern.compile(".*<script.*>.*</script>.*", Pattern.CASE_INSENSITIVE),
        Pattern.compile(".*javascript:.*", Pattern.CASE_INSENSITIVE),
        Pattern.compile(".*onload=.*", Pattern.CASE_INSENSITIVE),
        Pattern.compile(".*onerror=.*", Pattern.CASE_INSENSITIVE),
        Pattern.compile(".*onclick=.*", Pattern.CASE_INSENSITIVE)
    );

    // Path traversal patterns
    private static final List<Pattern> PATH_TRAVERSAL_PATTERNS = Arrays.asList(
        Pattern.compile(".*\\.\\..*"),
        Pattern.compile(".*%2e%2e.*", Pattern.CASE_INSENSITIVE),
        Pattern.compile(".*%252e%252e.*", Pattern.CASE_INSENSITIVE)
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        // Validate request path
        String path = request.getPath().value();
        if (containsMaliciousPatterns(path)) {
            return handleMaliciousRequest(exchange, "Malicious pattern detected in path");
        }
        
        // Validate query parameters
        request.getQueryParams().forEach((key, values) -> {
            values.forEach(value -> {
                if (containsMaliciousPatterns(value)) {
                    handleMaliciousRequest(exchange, "Malicious pattern detected in query parameter: " + key);
                }
            });
        });
        
        // Validate headers
        request.getHeaders().forEach((key, values) -> {
            values.forEach(value -> {
                if (containsMaliciousPatterns(value)) {
                    handleMaliciousRequest(exchange, "Malicious pattern detected in header: " + key);
                }
            });
        });
        
        return chain.filter(exchange);
    }

    private boolean containsMaliciousPatterns(String input) {
        if (input == null || input.isEmpty()) {
            return false;
        }
        
        // Check SQL injection patterns
        for (Pattern pattern : SQL_INJECTION_PATTERNS) {
            if (pattern.matcher(input).matches()) {
                return true;
            }
        }
        
        // Check XSS patterns
        for (Pattern pattern : XSS_PATTERNS) {
            if (pattern.matcher(input).matches()) {
                return true;
            }
        }
        
        // Check path traversal patterns
        for (Pattern pattern : PATH_TRAVERSAL_PATTERNS) {
            if (pattern.matcher(input).matches()) {
                return true;
            }
        }
        
        return false;
    }

    private Mono<Void> handleMaliciousRequest(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.BAD_REQUEST);
        
        String body = "{\"error\":\"Bad Request\",\"message\":\"" + message + "\"}";
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -3; // Highest priority
    }
}
