package com.forensic.auth.service;

import com.forensic.auth.dto.*;
import com.forensic.auth.entity.Role;
import com.forensic.auth.entity.User;
import com.forensic.auth.entity.UserStatus;
import com.forensic.auth.repository.UserRepository;
import com.forensic.auth.repository.UserSessionRepository;
import com.forensic.auth.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
// import java.util.UUID; // DÜZELTME: Artık String ID kullanıyoruz
import java.util.stream.Collectors;

/**
 * User Service
 * * Handles user-related operations including CRUD operations and business
 * logic
 */
@Service
@Transactional
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Find user by username
     */
    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Find user by email
     */
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Find user by ID
     */
    @Transactional(readOnly = true)
    // DÜZELTME: UUID -> String
    public Optional<User> findById(String userId) {
        return userRepository.findById(userId);
    }

    /**
     * Check if username exists
     */
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    /**
     * Check if email exists
     */
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * Create a new user
     */
    public User createUser(RegisterRequest registerRequest) {
        try {
            User user = new User();
            user.setUsername(registerRequest.getUsername());
            user.setEmail(registerRequest.getEmail());
            user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
            user.setFirstName(registerRequest.getFirstName());
            user.setLastName(registerRequest.getLastName());
            user.setStatus(UserStatus.ACTIVE);

            // Set default role if not specified
            if (registerRequest.getRoles() == null || registerRequest.getRoles().isEmpty()) {
                user.setRoles(Set.of(Role.VIEWER));
            } else {
                // DÜZELTME: List'i Set'e çevir
                user.setRoles(new HashSet<>(registerRequest.getRoles()));
            }

            User savedUser = userRepository.save(user);
            logger.info("User created successfully: {}", savedUser.getUsername());
            return savedUser;

        } catch (Exception e) {
            logger.error("Failed to create user: {}", e.getMessage());
            throw new RuntimeException("Failed to create user", e);
        }
    }

    /**
     * Update user profile
     */
    public User updateProfile(String username, UpdateProfileRequest updateRequest) {
        try {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            user.setFirstName(updateRequest.getFirstName());
            user.setLastName(updateRequest.getLastName());
            user.setEmail(updateRequest.getEmail());

            User updatedUser = userRepository.save(user);
            logger.info("Profile updated for user: {}", username);
            return updatedUser;

        } catch (Exception e) {
            logger.error("Failed to update profile for user {}: {}", username, e.getMessage());
            throw new RuntimeException("Failed to update profile", e);
        }
    }

    /**
     * Update user (admin only)
     */
    // DÜZELTME: UUID -> String
    public User updateUser(String userId, UpdateUserRequest updateRequest) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            user.setFirstName(updateRequest.getFirstName());
            user.setLastName(updateRequest.getLastName());
            user.setEmail(updateRequest.getEmail());
            user.setStatus(updateRequest.getStatus());

            // DÜZELTME: List'i Set'e çevir
            if (updateRequest.getRoles() != null) {
                user.setRoles(new HashSet<>(updateRequest.getRoles()));
            }

            User updatedUser = userRepository.save(user);
            logger.info("User updated: {}", updatedUser.getUsername());
            return updatedUser;

        } catch (Exception e) {
            logger.error("Failed to update user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to update user", e);
        }
    }

    /**
     * Change user password
     */
    public void changePassword(String username, ChangePasswordRequest changeRequest) {
        try {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Verify current password
            if (!passwordEncoder.matches(changeRequest.getCurrentPassword(), user.getPassword())) {
                throw new RuntimeException("Current password is incorrect");
            }

            // Verify new password confirmation
            if (!changeRequest.getNewPassword().equals(changeRequest.getConfirmPassword())) {
                throw new RuntimeException("New password and confirmation do not match");
            }

            // Update password
            user.setPassword(passwordEncoder.encode(changeRequest.getNewPassword()));

            userRepository.save(user);
            logger.info("Password changed for user: {}", username);

        } catch (Exception e) {
            logger.error("Failed to change password for user {}: {}", username, e.getMessage());
            throw new RuntimeException("Failed to change password", e);
        }
    }

    /**
     * Update last login time
     */
    // DÜZELTME: UUID -> String
    public void updateLastLogin(String userId) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            user.setLastLogin(LocalDateTime.now());
            user.resetFailedLoginAttempts(); // Reset failed attempts on successful login

            userRepository.save(user);

        } catch (Exception e) {
            logger.error("Failed to update last login for user {}: {}", userId, e.getMessage());
        }
    }

    /**
     * Increment failed login attempts
     */
    public void incrementFailedLoginAttempts(String username) {
        try {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            user.incrementFailedLoginAttempts();
            userRepository.save(user);

            logger.warn("Failed login attempt for user: {} (attempts: {})", username, user.getFailedLoginAttempts());

        } catch (Exception e) {
            logger.error("Failed to increment failed login attempts for user {}: {}", username, e.getMessage());
        }
    }

    /**
     * Get all users with pagination
     */
    @Transactional(readOnly = true)
    public List<UserInfo> getAllUsers(int page, int size, String sortBy, String sortDir) {
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

            Pageable pageable = PageRequest.of(page, size, sort);

            return userRepository.findAll(pageable)
                    .getContent()
                    .stream()
                    .map(this::convertToUserInfo)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            logger.error("Failed to get users: {}", e.getMessage());
            throw new RuntimeException("Failed to get users", e);
        }
    }

    /**
     * Delete user
     */
    // DÜZELTME: UUID -> String
    public void deleteUser(String userId) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Don't allow deletion of admin users
            if (user.getRoles().contains(Role.ADMIN)) {
                throw new RuntimeException("Cannot delete admin users");
            }

            userRepository.delete(user);
            logger.info("User deleted: {}", user.getUsername());

        } catch (Exception e) {
            logger.error("Failed to delete user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to delete user", e);
        }
    }

    /**
     * Convert User entity to UserInfo DTO
     */
    private UserInfo convertToUserInfo(User user) {
        return new UserInfo(
                user.getId(), // DÜZELTME: .toString() kaldırıldı, zaten String
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getStatus().name(),
                user.getRoles().stream().map(Role::name).collect(Collectors.toList()),
                user.isTwoFactorEnabled(),
                user.getLastLogin(),
                user.getCreatedAt(),
                user.getUpdatedAt());
    }

    /**
     * Authentication Service
     * * Handles authentication-related operations including session management
     */
    @Service
    @Transactional
    public class AuthService {

        private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

        private final AuthService service;
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

        public AuthService(AuthService service) {
            this.service = service;
        }


        // --- DÜZELTME: EKSİK METOTLARI EKLE ---

        /**
         * Authenticate user and return JWT
         */
        public LoginResponse authenticateUser(LoginRequest loginRequest, String ipAddress, String userAgent) {
            try {
                Authentication authentication = authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

                SecurityContextHolder.getContext().setAuthentication(authentication);

                UserDetailsServiceImpl.UserPrincipal userPrincipal = (UserDetailsServiceImpl.UserPrincipal) authentication.getPrincipal();
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

                String sessionId = service.createSession(
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

        /**
         * Register a new user
         */
        /**
         * Get user profile
         */
        /**
         * Update user profile
         */
        /**
         * Get all users
         */
        /**
         * Get user by ID
         */
        /**
         * Update user by ID
         */
        /**
         * Delete user by ID
         */
        public void deleteUserById(String id) {
            userRepository.deleteById(id);
        }

            User user = userRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            user.setFirstName(updateUserRequest.getFirstName());
            user.setLastName(updateUserRequest.getLastName());
            user.setEmail(updateUserRequest.getEmail());
            user.setUsername(updateUserRequest.getUsername());
            user.setStatus(UserStatus.valueOf(updateUserRequest.getStatus()));
            // Role update logic can be added here if needed

            return userRepository.save(user);
        }
}
