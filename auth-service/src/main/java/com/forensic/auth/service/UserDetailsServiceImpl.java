package com.forensic.auth.service;

import com.forensic.auth.entity.Role;
import com.forensic.auth.entity.User;
import com.forensic.auth.entity.UserStatus;
import com.forensic.auth.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * User Details Service Implementation
 * 
 * This service provides user details for Spring Security authentication
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.debug("Loading user by username: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    logger.warn("User not found with username: {}", username);
                    return new UsernameNotFoundException("User not found with username: " + username);
                });

        // Check if user account is active
        if (!user.getStatus().canLogin()) {
            logger.warn("User account is not active: {} (status: {})", username, user.getStatus());
            throw new UsernameNotFoundException("User account is not active: " + username);
        }

        // Check if account is locked
        if (user.isAccountLocked()) {
            logger.warn("User account is locked: {}", username);
            throw new UsernameNotFoundException("User account is locked: " + username);
        }

        logger.debug("User loaded successfully: {}", username);
        return createUserPrincipal(user);
    }

    /**
     * Create UserPrincipal from User entity
     */
    private UserDetails createUserPrincipal(User user) {
        return new UserPrincipal(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPassword(),
                user.getFirstName(),
                user.getLastName(),
                user.getStatus(),
                mapRolesToAuthorities(user.getRoles()),
                user.isTwoFactorEnabled(),
                user.getLastLogin(),
                user.getCreatedAt());
    }

    /**
     * Map roles to Spring Security authorities
     */
    private Collection<? extends GrantedAuthority> mapRolesToAuthorities(Set<Role> roles) {
        if (roles == null || roles.isEmpty()) {
            return Set.of(new SimpleGrantedAuthority("ROLE_VIEWER"));
        }

        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                .collect(Collectors.toSet());
    }

    /**
     * Custom UserDetails implementation
     */
    public static class UserPrincipal implements UserDetails {
        private final String id;
        private final String username;
        private final String email;
        private final String password;
        private final String firstName;
        private final String lastName;
        private final UserStatus status;
        private final Collection<? extends GrantedAuthority> authorities;
        private final boolean twoFactorEnabled;
        private final java.time.LocalDateTime lastLogin;
        private final java.time.LocalDateTime createdAt;

        public UserPrincipal(String id, String username, String email, String password,
                String firstName, String lastName, UserStatus status,
                Collection<? extends GrantedAuthority> authorities,
                boolean twoFactorEnabled, java.time.LocalDateTime lastLogin,
                java.time.LocalDateTime createdAt) {
            this.id = id;
            this.username = username;
            this.email = email;
            this.password = password;
            this.firstName = firstName;
            this.lastName = lastName;
            this.status = status;
            this.authorities = authorities;
            this.twoFactorEnabled = twoFactorEnabled;
            this.lastLogin = lastLogin;
            this.createdAt = createdAt;
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return authorities;
        }

        @Override
        public String getPassword() {
            return password;
        }

        @Override
        public String getUsername() {
            return username;
        }

        @Override
        public boolean isAccountNonExpired() {
            return status != UserStatus.EXPIRED;
        }

        @Override
        public boolean isAccountNonLocked() {
            return status != UserStatus.LOCKED;
        }

        @Override
        public boolean isCredentialsNonExpired() {
            // Password expiration can be implemented here
            return true;
        }

        @Override
        public boolean isEnabled() {
            return status == UserStatus.ACTIVE;
        }

        // Getters for additional user information
        public String getId() {
            return id;
        }

        public String getEmail() {
            return email;
        }

        public String getFirstName() {
            return firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public String getFullName() {
            return firstName + " " + lastName;
        }

        public UserStatus getStatus() {
            return status;
        }

        public boolean isTwoFactorEnabled() {
            return twoFactorEnabled;
        }

        public java.time.LocalDateTime getLastLogin() {
            return lastLogin;
        }

        public java.time.LocalDateTime getCreatedAt() {
            return createdAt;
        }

        /**
         * Check if user has a specific role
         */
        public boolean hasRole(String role) {
            return authorities.stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_" + role));
        }

        /**
         * Check if user has admin role
         */
        public boolean isAdmin() {
            return hasRole("ADMIN");
        }

        /**
         * Check if user has investigator role
         */
        public boolean isInvestigator() {
            return hasRole("INVESTIGATOR");
        }

        /**
         * Check if user has analyst role
         */
        public boolean isAnalyst() {
            return hasRole("ANALYST");
        }
    }
}
