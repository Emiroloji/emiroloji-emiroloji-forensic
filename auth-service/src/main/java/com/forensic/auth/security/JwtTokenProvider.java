package com.forensic.auth.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JWT Token Provider for generating and validating JWT tokens
 * 
 * This class provides:
 * - Token generation with custom claims
 * - Token validation and parsing
 * - User information extraction from tokens
 * - Token expiration handling
 */
@Component
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    @Value("${jwt.secret:forensic_jwt_secret_key_2024_very_long_and_secure_change_this_to_256_bit_key}")
    private String jwtSecret;

    @Value("${jwt.expiration:86400000}") // 24 hours in milliseconds
    private int jwtExpirationMs;

    @Value("${jwt.refresh-expiration:604800000}") // 7 days in milliseconds
    private int jwtRefreshExpirationMs;

    /**
     * Generate JWT token from authentication
     */
    public String generateToken(Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        return generateTokenFromUsername(userPrincipal.getUsername());
    }

    /**
     * Generate JWT token from authentication
     */
    public String generateJwtToken(Authentication authentication) {
        return generateToken(authentication);
    }

    /**
     * Generate JWT token from username with custom claims
     */
    public String generateTokenFromUsername(String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "access");
        claims.put("issued_at", System.currentTimeMillis());
        return createToken(claims, username, jwtExpirationMs);
    }

    /**
     * Generate refresh token
     */
    public String generateRefreshToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "refresh");
        claims.put("issued_at", System.currentTimeMillis());
        return createToken(claims, username, jwtRefreshExpirationMs);
    }

    /**
     * Generate refresh token
     */
    public String generateRefreshTokenFromUsername(String username) {
        return generateRefreshToken(username);
    }

    /**
     * Create JWT token with claims
     */
    private String createToken(Map<String, Object> claims, String subject, long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .setIssuer("forensic-face-match-system")
                .setAudience("forensic-clients")
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * Get signing key from secret
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Extract username from token
     */
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    /**
     * Extract username from token
     */
    public String getUserNameFromJwtToken(String token) {
        return getUsernameFromToken(token);
    }

    /**
     * Extract expiration date from token
     */
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    /**
     * Extract claim from token
     */
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extract all claims from token
     */
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Check if token is expired
     */
    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    /**
     * Validate token
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String username = getUsernameFromToken(token);
            return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        } catch (JwtException | IllegalArgumentException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Validate token without user details
     */
    public Boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("JWT token validation failed: {}", e.getMessage());
        }
        return false;
    }

    /**
     * Validate token without user details
     */
    public Boolean validateJwtToken(String token) {
        return validateToken(token);
    }

    /**
     * Get token type (access or refresh)
     */
    public String getTokenType(String token) {
        return getClaimFromToken(token, claims -> claims.get("type", String.class));
    }

    /**
     * Check if token is access token
     */
    public Boolean isAccessToken(String token) {
        return "access".equals(getTokenType(token));
    }

    /**
     * Check if token is refresh token
     */
    public Boolean isRefreshToken(String token) {
        return "refresh".equals(getTokenType(token));
    }

    /**
     * Get time until token expires in milliseconds
     */
    public Long getTimeUntilExpiration(String token) {
        try {
            Date expiration = getExpirationDateFromToken(token);
            return expiration.getTime() - System.currentTimeMillis();
        } catch (Exception e) {
            logger.error("Error getting token expiration time: {}", e.getMessage());
            return 0L;
        }
    }

    /**
     * Check if token will expire soon (within 5 minutes)
     */
    public Boolean isTokenExpiringSoon(String token) {
        Long timeUntilExpiration = getTimeUntilExpiration(token);
        return timeUntilExpiration > 0 && timeUntilExpiration < 300000; // 5 minutes
    }

    /**
     * Extract all claims as map
     */
    public Map<String, Object> getAllClaims(String token) {
        try {
            return getAllClaimsFromToken(token);
        } catch (Exception e) {
            logger.error("Error extracting claims from token: {}", e.getMessage());
            return new HashMap<>();
        }
    }

    /**
     * Get token issuer
     */
    public String getTokenIssuer(String token) {
        return getClaimFromToken(token, Claims::getIssuer);
    }

    /**
     * Get token audience
     */
    public String getTokenAudience(String token) {
        return getClaimFromToken(token, Claims::getAudience);
    }

    /**
     * Get token issued at time
     */
    public Date getTokenIssuedAt(String token) {
        return getClaimFromToken(token, Claims::getIssuedAt);
    }
}
