package com.forensic.auth.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private Authentication authentication;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", 
            "YourSuperSecretJwtKeyThatIsAtLeast256BitsLongAndShouldBeRandomlyGeneratedInProduction");
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationMs", 3600000);
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtRefreshExpirationMs", 604800000);

        authentication = mock(Authentication.class);
        userDetails = mock(UserDetails.class);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("testuser");
    }

    @Test
    void testGenerateJwtToken() {
        // When
        String token = jwtTokenProvider.generateJwtToken(authentication);

        // Then
        assertNotNull(token);
        assertTrue(token.length() > 0);
    }

    @Test
    void testGenerateTokenFromUsername() {
        // When
        String token = jwtTokenProvider.generateTokenFromUsername("testuser");

        // Then
        assertNotNull(token);
        assertTrue(token.length() > 0);
    }

    @Test
    void testGenerateRefreshTokenFromUsername() {
        // When
        String refreshToken = jwtTokenProvider.generateRefreshTokenFromUsername("testuser");

        // Then
        assertNotNull(refreshToken);
        assertTrue(refreshToken.length() > 0);
    }

    @Test
    void testGetUserNameFromJwtToken() {
        // Given
        String token = jwtTokenProvider.generateTokenFromUsername("testuser");

        // When
        String username = jwtTokenProvider.getUserNameFromJwtToken(token);

        // Then
        assertEquals("testuser", username);
    }

    @Test
    void testValidateJwtToken_ValidToken() {
        // Given
        String token = jwtTokenProvider.generateTokenFromUsername("testuser");

        // When
        boolean isValid = jwtTokenProvider.validateJwtToken(token);

        // Then
        assertTrue(isValid);
    }

    @Test
    void testValidateJwtToken_InvalidToken() {
        // Given
        String invalidToken = "invalid.token.here";

        // When
        boolean isValid = jwtTokenProvider.validateJwtToken(invalidToken);

        // Then
        assertFalse(isValid);
    }

    @Test
    void testValidateJwtToken_ExpiredToken() {
        // Given
        JwtTokenProvider expiredTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(expiredTokenProvider, "jwtSecret", 
            "YourSuperSecretJwtKeyThatIsAtLeast256BitsLongAndShouldBeRandomlyGeneratedInProduction");
        ReflectionTestUtils.setField(expiredTokenProvider, "jwtExpirationMs", -1); // Expired immediately

        String expiredToken = expiredTokenProvider.generateTokenFromUsername("testuser");

        // When
        boolean isValid = jwtTokenProvider.validateJwtToken(expiredToken);

        // Then
        assertFalse(isValid);
    }
}
