package com.forensic.auth.repository;

import com.forensic.auth.entity.Role;
import com.forensic.auth.entity.User;
import com.forensic.auth.entity.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * User Repository Interface
 * 
 * Provides data access methods for User entities
 */
@Repository
public interface UserRepository extends JpaRepository<User, String> {
        /**
         * Find user by username
         */
        Optional<User> findByUsername(String username);

        /**
         * Find user by email
         */
        Optional<User> findByEmail(String email);

        /**
         * Find user by username or email
         */
        @Query("SELECT u FROM User u WHERE u.username = :usernameOrEmail OR u.email = :usernameOrEmail")
        Optional<User> findByUsernameOrEmail(@Param("usernameOrEmail") String usernameOrEmail);

        /**
         * Check if username exists
         */
        boolean existsByUsername(String username);

        /**
         * Check if email exists
         */
        boolean existsByEmail(String email);

        /**
         * Find users by status
         */
        List<User> findByStatus(UserStatus status);

        /**
         * Find users by role
         */
        @Query("SELECT u FROM User u JOIN u.roles r WHERE r = :role")
        List<User> findByRole(@Param("role") Role role);

        /**
         * Find users with multiple roles
         */
        @Query("SELECT u FROM User u JOIN u.roles r WHERE r IN :roles")
        List<User> findByRolesIn(@Param("roles") List<Role> roles);

        /**
         * Find active users
         */
        @Query("SELECT u FROM User u WHERE u.status = 'ACTIVE'")
        List<User> findActiveUsers();

        /**
         * Find locked users
         */
        @Query("SELECT u FROM User u WHERE u.accountLockedUntil IS NOT NULL AND u.accountLockedUntil > :now")
        List<User> findLockedUsers(@Param("now") LocalDateTime now);

        /**
         * Find users with failed login attempts
         */
        @Query("SELECT u FROM User u WHERE u.failedLoginAttempts > 0")
        List<User> findUsersWithFailedLoginAttempts();

        /**
         * Find users created after a specific date
         */
        List<User> findByCreatedAtAfter(LocalDateTime date);

        /**
         * Find users by department (if implemented)
         */
        // List<User> findByDepartment(String department);

        /**
         * Count users by status
         */
        long countByStatus(UserStatus status);

        /**
         * Count users by role
         */
        @Query("SELECT COUNT(u) FROM User u JOIN u.roles r WHERE r = :role")
        long countByRole(@Param("role") Role role);

        /**
         * Find users with 2FA enabled
         */
        List<User> findByTwoFactorEnabledTrue();

        /**
         * Find users with 2FA disabled
         */
        List<User> findByTwoFactorEnabledFalse();

        /**
         * Find users who haven't logged in recently
         */
        @Query("SELECT u FROM User u WHERE u.lastLogin IS NULL OR u.lastLogin < :date")
        List<User> findUsersNotLoggedInSince(@Param("date") LocalDateTime date);

        /**
         * Find users with expired passwords (if password expiration is implemented)
         */
        @Query("SELECT u FROM User u WHERE u.passwordChangedAt < :date")
        List<User> findUsersWithExpiredPasswords(@Param("date") LocalDateTime date);

        /**
         * Search users by name
         */
        @Query("SELECT u FROM User u WHERE LOWER(u.firstName) LIKE LOWER(CONCAT('%', :name, '%')) OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :name, '%'))")
        List<User> findByNameContaining(@Param("name") String name);

        /**
         * Find users with specific role and status
         */
        @Query("SELECT u FROM User u JOIN u.roles r WHERE r = :role AND u.status = :status")
        List<User> findByRoleAndStatus(@Param("role") Role role, @Param("status") UserStatus status);

        /**
         * Find users created by a specific user (if implemented)
         */
        // @Query("SELECT u FROM User u WHERE u.createdBy = :createdBy")
        // List<User> findByCreatedBy(@Param("createdBy") UUID createdBy);

        /**
         * Find users with specific permissions (custom query based on role permissions)
         */
        @Query("SELECT DISTINCT u FROM User u JOIN u.roles r WHERE r IN (" +
                        "SELECT r2 FROM Role r2 WHERE r2 IN ('ADMIN', 'INVESTIGATOR'))")
        List<User> findUsersWithCaseManagementPermissions();

        /**
         * Find users with analysis permissions
         */
        @Query("SELECT DISTINCT u FROM User u JOIN u.roles r WHERE r IN (" +
                        "SELECT r2 FROM Role r2 WHERE r2 IN ('ADMIN', 'INVESTIGATOR', 'ANALYST'))")
        List<User> findUsersWithAnalysisPermissions();

        /**
         * Find users with audit permissions
         */
        @Query("SELECT DISTINCT u FROM User u JOIN u.roles r WHERE r IN (" +
                        "SELECT r2 FROM Role r2 WHERE r2 IN ('ADMIN', 'AUDITOR'))")
        List<User> findUsersWithAuditPermissions();

}
