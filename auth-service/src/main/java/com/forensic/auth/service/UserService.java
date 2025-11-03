package com.forensic.auth.service;

import com.forensic.auth.dto.*;
import com.forensic.auth.entity.Role;
import com.forensic.auth.entity.User;
import com.forensic.auth.entity.UserStatus;
import com.forensic.auth.repository.UserRepository;
import com.forensic.auth.repository.UserSessionRepository; // Bu import hatalı kod bloğundan kalmış olabilir, ama UserService'te direkt kullanılmıyor. Zararı yok.
import com.forensic.auth.security.JwtTokenProvider; // Bu import hatalı kod bloğundan kalmış olabilir, ama UserService'te direkt kullanılmıyor. Zararı yok.
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.AuthenticationManager; // Bu import hatalı kod bloğundan kalmış olabilir.
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken; // Bu import hatalı kod bloğundan kalmış olabilir.
import org.springframework.security.core.Authentication; // Bu import hatalı kod bloğundan kalmış olabilir.
import org.springframework.security.core.GrantedAuthority; // Bu import hatalı kod bloğundan kalmış olabilir.
import org.springframework.security.core.context.SecurityContextHolder; // Bu import hatalı kod bloğundan kalmış olabilir.
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

    // --- HATALI KOD BLOĞU (AuthService Sınıfı) BURADAN KALDIRILDI ---

}