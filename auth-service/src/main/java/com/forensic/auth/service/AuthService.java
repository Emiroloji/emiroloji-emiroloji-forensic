package com.forensic.auth.service;


import com.forensic.auth.dto.*;
import com.forensic.auth.entity.Role;
import com.forensic.auth.entity.User;
import com.forensic.auth.entity.UserStatus;
import com.forensic.auth.entity.UserSession;
import com.forensic.auth.repository.UserRepository;
import com.forensic.auth.repository.UserSessionRepository;
import com.forensic.auth.security.JwtTokenProvider;
import com.forensic.auth.service.UserDetailsServiceImpl.UserPrincipal;
import com.forensic.auth.service.UserDetailsServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Authentication Service
 * * Handles authentication-related operations including session management
 */
@Service
@Transactional
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private UserSessionRepository userSessionRepository;


    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;


    // --- Authentication ---

    /**
     * Authenticate user and return JWT
     */
    public LoginResponse authenticateUser(LoginRequest loginRequest, String ipAddress, String userAgent) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);

            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            String accessToken = jwtTokenProvider.generateToken(authentication);
            String refreshToken = jwtTokenProvider.generateRefreshToken(userPrincipal.getUsername());

            User user = userRepository.findById(userPrincipal.getId())
                    .orElseThrow(() -> new RuntimeException("User not found after authentication"));

            UUID userId;
            try {
                userId = UUID.fromString(user.getId());
            } catch (IllegalArgumentException e) {
                logger.error("Invalid UUID format for user ID: {}", user.getId());
                throw new RuntimeException("Invalid user ID format", e);
            }

            String sessionId = createSession(
                    userId,
                    accessToken,
                    refreshToken,
                    ipAddress,
                    userAgent
            );

            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);

            List<String> roles = userPrincipal.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());

            LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo(
                    userPrincipal.getId(),
                    userPrincipal.getUsername(),
                    userPrincipal.getEmail(),
                    userPrincipal.getFirstName(),
                    userPrincipal.getLastName(),
                    userPrincipal.getStatus().name(),
                    roles,
                    userPrincipal.isTwoFactorEnabled(),
                    userPrincipal.getLastLogin());

            return new LoginResponse(
                    accessToken,
                    refreshToken,
                    "Bearer",
                    jwtTokenProvider.getTimeUntilExpiration(accessToken),
                    userInfo,
                    sessionId);

        } catch (Exception e) {
            logger.error("Authentication failed for user {}: {}", loginRequest.getUsername(), e.getMessage());
            throw new RuntimeException("Invalid credentials", e);
        }
    }

    // --- User / Registration / Profile related methods ---

    public User registerUser(RegisterRequest registerRequest) {
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new RuntimeException("Error: Username is already taken!");
        }

        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new RuntimeException("Error: Email is already in use!");
        }

        // Create new user's account
        User user = new User(
                registerRequest.getUsername(),
                registerRequest.getEmail(),
                passwordEncoder.encode(registerRequest.getPassword()),
                registerRequest.getFirstName(),
                registerRequest.getLastName());

        // Set roles
        Set<Role> roles = new HashSet<>();
        if (registerRequest.getRoles() == null || registerRequest.getRoles().isEmpty()) {
            roles.add(Role.VIEWER);
        } else {
            roles.addAll(registerRequest.getRoles());
        }
        user.setRoles(roles);
        user.setStatus(UserStatus.ACTIVE);

        User savedUser = userRepository.save(user);
        logger.info("User registered successfully: {}", savedUser.getUsername());
        return savedUser;
    }

    public LoginResponse refreshToken(RefreshTokenRequest refreshTokenRequest) {
        String refreshToken = refreshTokenRequest.getRefreshToken();

        if (!jwtTokenProvider.validateToken(refreshToken) || !jwtTokenProvider.isRefreshToken(refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }

        String username = jwtTokenProvider.getUsernameFromToken(refreshToken);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String newAccessToken = jwtTokenProvider.generateTokenFromUsername(username);

        userSessionRepository.findByRefreshToken(refreshToken)
                .ifPresent(session -> {
                    session.setSessionToken(newAccessToken);
                    userSessionRepository.save(session);
                });

        // Build a UserDetails-like object from User entity (using existing UserPrincipal)
        UserDetails userDetails = new UserDetailsServiceImpl.UserPrincipal(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPassword(),
                user.getFirstName(),
                user.getLastName(),
                user.getStatus(),
                user.getRoles().stream().map(role -> (GrantedAuthority) () -> "ROLE_" + role.name()).collect(Collectors.toList()),
                user.isTwoFactorEnabled(),
                user.getLastLogin(),
                user.getCreatedAt()
        );

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getStatus().name(),
                roles,
                user.isTwoFactorEnabled(),
                user.getLastLogin());

        String sessionId = userSessionRepository.findByRefreshToken(refreshToken)
                .map(s -> s.getId().toString())
                .orElse(null);

        return new LoginResponse(
                newAccessToken,
                refreshToken,
                "Bearer",
                jwtTokenProvider.getTimeUntilExpiration(newAccessToken),
                userInfo,
                sessionId);
    }

    public LoginResponse.UserInfo getUserProfile(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<String> roles = user.getRoles().stream()
                .map(role -> "ROLE_" + role.name())
                .collect(Collectors.toList());

        return new LoginResponse.UserInfo(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getStatus().name(),
                roles,
                user.isTwoFactorEnabled(),
                user.getLastLogin());
    }

    public User updateUserProfile(String userId, UpdateProfileRequest updateProfileRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setFirstName(updateProfileRequest.getFirstName());
        user.setLastName(updateProfileRequest.getLastName());
        user.setEmail(updateProfileRequest.getEmail());
        // Password updates should be separate

        return userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public User updateUserById(String id, UpdateUserRequest updateUserRequest) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setFirstName(updateUserRequest.getFirstName());
        user.setLastName(updateUserRequest.getLastName());
        user.setEmail(updateUserRequest.getEmail());
        user.setUsername(updateUserRequest.getUsername());
        user.setStatus(UserStatus.valueOf(updateUserRequest.getStatus()));
        if (updateUserRequest.getRoles() != null) {
            user.setRoles(new HashSet<>(updateUserRequest.getRoles()));
        }

        return userRepository.save(user);
    }

    public void deleteUserById(String id) {
        userRepository.deleteById(id);
    }

    // --- Session management ---

    /**
     * Create a new user session
     */
    public String createSession(UUID userId, String accessToken, String refreshToken,
            String ipAddress, String userAgent) {
        try {
            // Calculate expiration time (24 hours from now)
            LocalDateTime expiresAt = LocalDateTime.now().plusHours(24);

            // Create session entity
            UserSession session = new UserSession();
            session.setUserId(userId);
            session.setSessionToken(accessToken);
            session.setRefreshToken(refreshToken);
            session.setIpAddress(ipAddress);
            session.setUserAgent(userAgent);
            session.setExpiresAt(expiresAt);

            // Save session
            UserSession savedSession = userSessionRepository.save(session);

            logger.info("Session created for user {} with ID {}", userId, savedSession.getId());
            return savedSession.getId().toString();

        } catch (Exception e) {
            logger.error("Failed to create session for user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to create session", e);
        }
    }

    /**
     * Invalidate a session
     */
    public void invalidateSession(String token) {
        try {
            // Find session by token
            userSessionRepository.findBySessionToken(token)
                    .ifPresent(session -> {
                        // Mark session as expired
                        session.setExpiresAt(LocalDateTime.now().minusMinutes(1));
                        userSessionRepository.save(session);
                        logger.info("Session invalidated for user {}", session.getUserId());
                    });

        } catch (Exception e) {
            logger.error("Failed to invalidate session: {}", e.getMessage());
            throw new RuntimeException("Failed to invalidate session", e);
        }
    }

    /**
     * Validate session
     */
    public boolean validateSession(String token) {
        try {
            return userSessionRepository.findBySessionToken(token)
                    .map(session -> session.getExpiresAt().isAfter(LocalDateTime.now()))
                    .orElse(false);

        } catch (Exception e) {
            logger.error("Failed to validate session: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Clean up expired sessions
     */
    public void cleanupExpiredSessions() {
        try {
            LocalDateTime now = LocalDateTime.now();
            long deletedCount = userSessionRepository.deleteByExpiresAtBefore(now);
            logger.info("Cleaned up {} expired sessions", deletedCount);

        } catch (Exception e) {
            logger.error("Failed to cleanup expired sessions: {}", e.getMessage());
        }
    }

    /**
     * Get active sessions for a user
     */
    public long getActiveSessionCount(UUID userId) {
        try {
            return userSessionRepository.countByUserIdAndExpiresAtAfter(userId, LocalDateTime.now());

        } catch (Exception e) {
            logger.error("Failed to get active session count for user {}: {}", userId, e.getMessage());
            return 0;
        }
    }

    /**
     * Invalidate all sessions for a user
     */
    public void invalidateAllUserSessions(UUID userId) {
        try {
            LocalDateTime now = LocalDateTime.now();
            userSessionRepository.findByUserIdAndExpiresAtAfter(userId, now)
                    .forEach(session -> {
                        session.setExpiresAt(now.minusMinutes(1));
                        userSessionRepository.save(session);
                    });

            logger.info("All sessions invalidated for user {}", userId);

        } catch (Exception e) {
            logger.error("Failed to invalidate all sessions for user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to invalidate all sessions", e);
        }
    }
}
