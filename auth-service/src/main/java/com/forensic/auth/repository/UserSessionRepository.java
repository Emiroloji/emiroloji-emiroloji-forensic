package com.forensic.auth.repository;

import com.forensic.auth.entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * User Session Repository Interface
 * 
 * Provides data access methods for UserSession entities
 */
@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, UUID> {

    /**
     * Find session by session token
     */
    Optional<UserSession> findBySessionToken(String sessionToken);

    /**
     * Find session by refresh token
     */
    Optional<UserSession> findByRefreshToken(String refreshToken);

    /**
     * Find all sessions for a user
     */
    List<UserSession> findByUserId(UUID userId);

    /**
     * Find active sessions for a user
     */
    @Query("SELECT s FROM UserSession s WHERE s.userId = :userId AND s.expiresAt > :now")
    List<UserSession> findByUserIdAndExpiresAtAfter(@Param("userId") UUID userId, @Param("now") LocalDateTime now);

    /**
     * Count active sessions for a user
     */
    @Query("SELECT COUNT(s) FROM UserSession s WHERE s.userId = :userId AND s.expiresAt > :now")
    long countByUserIdAndExpiresAtAfter(@Param("userId") UUID userId, @Param("now") LocalDateTime now);

    /**
     * Find expired sessions
     */
    @Query("SELECT s FROM UserSession s WHERE s.expiresAt < :now")
    List<UserSession> findExpiredSessions(@Param("now") LocalDateTime now);

    /**
     * Delete expired sessions
     */
    @Modifying
    @Query("DELETE FROM UserSession s WHERE s.expiresAt < :now")
    long deleteByExpiresAtBefore(@Param("now") LocalDateTime now);

    /**
     * Find sessions by IP address
     */
    List<UserSession> findByIpAddress(String ipAddress);

    /**
     * Find sessions created after a specific date
     */
    List<UserSession> findByCreatedAtAfter(LocalDateTime date);

    /**
     * Find sessions by user agent pattern
     */
    @Query("SELECT s FROM UserSession s WHERE s.userAgent LIKE %:userAgent%")
    List<UserSession> findByUserAgentContaining(@Param("userAgent") String userAgent);

    /**
     * Find sessions for a user created after a specific date
     */
    @Query("SELECT s FROM UserSession s WHERE s.userId = :userId AND s.createdAt > :date")
    List<UserSession> findByUserIdAndCreatedAtAfter(@Param("userId") UUID userId, @Param("date") LocalDateTime date);

    /**
     * Delete all sessions for a user
     */
    @Modifying
    @Query("DELETE FROM UserSession s WHERE s.userId = :userId")
    long deleteByUserId(@Param("userId") UUID userId);

    /**
     * Delete specific session
     */
    @Modifying
    @Query("DELETE FROM UserSession s WHERE s.sessionToken = :sessionToken")
    long deleteBySessionToken(@Param("sessionToken") String sessionToken);

    /**
     * Update last accessed time for a session
     */
    @Modifying
    @Query("UPDATE UserSession s SET s.lastAccessed = :lastAccessed WHERE s.sessionToken = :sessionToken")
    void updateLastAccessed(@Param("sessionToken") String sessionToken,
            @Param("lastAccessed") LocalDateTime lastAccessed);

    /**
     * Find sessions that haven't been accessed recently
     */
    @Query("SELECT s FROM UserSession s WHERE s.lastAccessed < :date OR s.lastAccessed IS NULL")
    List<UserSession> findInactiveSessions(@Param("date") LocalDateTime date);

    /**
     * Count total active sessions
     */
    @Query("SELECT COUNT(s) FROM UserSession s WHERE s.expiresAt > :now")
    long countActiveSessions(@Param("now") LocalDateTime now);

    /**
     * Find sessions by date range
     */
    @Query("SELECT s FROM UserSession s WHERE s.createdAt BETWEEN :startDate AND :endDate")
    List<UserSession> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Find sessions for a user with specific IP
     */
    @Query("SELECT s FROM UserSession s WHERE s.userId = :userId AND s.ipAddress = :ipAddress")
    List<UserSession> findByUserIdAndIpAddress(@Param("userId") UUID userId, @Param("ipAddress") String ipAddress);
}
