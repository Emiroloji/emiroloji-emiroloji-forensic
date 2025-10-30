package com.forensic.audit.repository;

import com.forensic.audit.entity.AuditLog;
import com.forensic.audit.entity.AuditLogStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Audit Log Repository Interface
 * 
 * Provides data access methods for AuditLog entities in MongoDB
 */
@Repository
public interface AuditLogRepository extends MongoRepository<AuditLog, String> {

        /**
         * Find audit logs by user ID
         */
        Page<AuditLog> findByUserId(String userId, Pageable pageable);

        /**
         * Find audit logs by event type
         */
        Page<AuditLog> findByEventType(String eventType, Pageable pageable);

        /**
         * Find audit logs by service name
         */
        Page<AuditLog> findByServiceName(String serviceName, Pageable pageable);

        /**
         * Find audit logs by resource
         */
        Page<AuditLog> findByResource(String resource, Pageable pageable);

        /**
         * Find audit logs by status
         */
        Page<AuditLog> findByStatus(AuditLogStatus status, Pageable pageable);

        /**
         * Find audit logs by timestamp range
         */
        Page<AuditLog> findByTimestampBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

        /**
         * Find audit logs by chain ID
         */
        Page<AuditLog> findByChainId(String chainId, Pageable pageable);

        /**
         * Find audit logs by chain ID ordered by chain index
         */
        List<AuditLog> findByChainIdOrderByChainIndex(String chainId);

        /**
         * Find audit logs by IP address
         */
        Page<AuditLog> findByIpAddress(String ipAddress, Pageable pageable);

        /**
         * Find audit logs by session ID
         */
        Page<AuditLog> findBySessionId(String sessionId, Pageable pageable);

        /**
         * Find audit logs by request ID
         */
        Page<AuditLog> findByRequestId(String requestId, Pageable pageable);

        /**
         * Find audit logs by correlation ID
         */
        Page<AuditLog> findByCorrelationId(String correlationId, Pageable pageable);

        /**
         * Find audit logs by user ID and event type
         */
        Page<AuditLog> findByUserIdAndEventType(String userId, String eventType, Pageable pageable);

        /**
         * Find audit logs by user ID and service name
         */
        Page<AuditLog> findByUserIdAndServiceName(String userId, String serviceName, Pageable pageable);

        /**
         * Find audit logs by user ID and status
         */
        Page<AuditLog> findByUserIdAndStatus(String userId, AuditLogStatus status, Pageable pageable);

        /**
         * Find audit logs by service name and event type
         */
        Page<AuditLog> findByServiceNameAndEventType(String serviceName, String eventType, Pageable pageable);

        /**
         * Find audit logs by service name and status
         */
        Page<AuditLog> findByServiceNameAndStatus(String serviceName, AuditLogStatus status, Pageable pageable);

        /**
         * Find audit logs by event type and status
         */
        Page<AuditLog> findByEventTypeAndStatus(String eventType, AuditLogStatus status, Pageable pageable);

        /**
         * Find audit logs by resource and resource ID
         */
        Page<AuditLog> findByResourceAndResourceId(String resource, String resourceId, Pageable pageable);

        /**
         * Find audit logs by user ID and timestamp range
         */
        Page<AuditLog> findByUserIdAndTimestampBetween(String userId, LocalDateTime startDate, LocalDateTime endDate,
                        Pageable pageable);

        /**
         * Find audit logs by service name and timestamp range
         */
        Page<AuditLog> findByServiceNameAndTimestampBetween(String serviceName, LocalDateTime startDate,
                        LocalDateTime endDate, Pageable pageable);

        /**
         * Find audit logs by event type and timestamp range
         */
        Page<AuditLog> findByEventTypeAndTimestampBetween(String eventType, LocalDateTime startDate,
                        LocalDateTime endDate,
                        Pageable pageable);

        /**
         * Find audit logs by status and timestamp range
         */
        Page<AuditLog> findByStatusAndTimestampBetween(AuditLogStatus status, LocalDateTime startDate,
                        LocalDateTime endDate, Pageable pageable);

        /**
         * Find audit logs by multiple criteria
         */
        @Query("{ $and: [" +
                        "{ $or: [{ 'userId': ?0 }, { $expr: { $eq: [?0, null] } }] }," +
                        "{ $or: [{ 'eventType': ?1 }, { $expr: { $eq: [?1, null] } }] }," +
                        "{ $or: [{ 'serviceName': ?2 }, { $expr: { $eq: [?2, null] } }] }," +
                        "{ $or: [{ 'resource': ?3 }, { $expr: { $eq: [?3, null] } }] }," +
                        "{ $or: [{ 'status': ?4 }, { $expr: { $eq: [?4, null] } }] }," +
                        "{ $or: [{ 'timestamp': { $gte: ?5 } }, { $expr: { $eq: [?5, null] } }] }," +
                        "{ $or: [{ 'timestamp': { $lte: ?6 } }, { $expr: { $eq: [?6, null] } }] }" +
                        "] }")
        Page<AuditLog> findByMultipleCriteria(String userId, String eventType, String serviceName,
                        String resource, AuditLogStatus status,
                        LocalDateTime startDate, LocalDateTime endDate,
                        Pageable pageable);

        /**
         * Find audit logs with error messages
         */
        @Query("{ 'errorMessage': { $exists: true, $ne: null } }")
        Page<AuditLog> findAuditLogsWithErrors(Pageable pageable);

        /**
         * Find audit logs by error code
         */
        Page<AuditLog> findByErrorCode(String errorCode, Pageable pageable);

        /**
         * Find audit logs by user agent
         */
        Page<AuditLog> findByUserAgentContaining(String userAgent, Pageable pageable);

        /**
         * Find audit logs by action
         */
        Page<AuditLog> findByAction(String action, Pageable pageable);

        /**
         * Find audit logs by action containing
         */
        Page<AuditLog> findByActionContaining(String action, Pageable pageable);

        /**
         * Find audit logs by resource ID
         */
        Page<AuditLog> findByResourceId(String resourceId, Pageable pageable);

        /**
         * Find audit logs by hash
         */
        Optional<AuditLog> findByHash(String hash);

        /**
         * Find audit logs by previous hash
         */
        Page<AuditLog> findByPreviousHash(String previousHash, Pageable pageable);

        /**
         * Find audit logs by chain index
         */
        Page<AuditLog> findByChainIndex(Integer chainIndex, Pageable pageable);

        /**
         * Find audit logs by chain index range
         */
        Page<AuditLog> findByChainIndexBetween(Integer startIndex, Integer endIndex, Pageable pageable);

        /**
         * Find the last audit log in a chain
         */
        Optional<AuditLog> findTopByOrderByChainIndexDesc();

        /**
         * Find the first audit log in a chain
         */
        Optional<AuditLog> findTopByOrderByChainIndexAsc();

        /**
         * Find audit logs by chain ID and chain index range
         */
        Page<AuditLog> findByChainIdAndChainIndexBetween(String chainId, Integer startIndex, Integer endIndex,
                        Pageable pageable);

        /**
         * Find audit logs by chain ID and chain index
         */
        Optional<AuditLog> findByChainIdAndChainIndex(String chainId, Integer chainIndex);

        /**
         * Find audit logs by chain ID and user ID
         */
        Page<AuditLog> findByChainIdAndUserId(String chainId, String userId, Pageable pageable);

        /**
         * Find audit logs by chain ID and service name
         */
        Page<AuditLog> findByChainIdAndServiceName(String chainId, String serviceName, Pageable pageable);

        /**
         * Find audit logs by chain ID and event type
         */
        Page<AuditLog> findByChainIdAndEventType(String chainId, String eventType, Pageable pageable);

        /**
         * Find audit logs by chain ID and status
         */
        Page<AuditLog> findByChainIdAndStatus(String chainId, AuditLogStatus status, Pageable pageable);

        /**
         * Find audit logs by chain ID and timestamp range
         */
        Page<AuditLog> findByChainIdAndTimestampBetween(String chainId, LocalDateTime startDate, LocalDateTime endDate,
                        Pageable pageable);

        /**
         * Count audit logs by user ID
         */
        long countByUserId(String userId);

        /**
         * Count audit logs by event type
         */
        long countByEventType(String eventType);

        /**
         * Count audit logs by service name
         */
        long countByServiceName(String serviceName);

        /**
         * Count audit logs by resource
         */
        long countByResource(String resource);

        /**
         * Count audit logs by status
         */
        long countByStatus(AuditLogStatus status);

        /**
         * Count audit logs by chain ID
         */
        long countByChainId(String chainId);

        /**
         * Count audit logs by IP address
         */
        long countByIpAddress(String ipAddress);

        /**
         * Count audit logs by session ID
         */
        long countBySessionId(String sessionId);

        /**
         * Count audit logs by request ID
         */
        long countByRequestId(String requestId);

        /**
         * Count audit logs by correlation ID
         */
        long countByCorrelationId(String correlationId);

        /**
         * Count audit logs by user ID and event type
         */
        long countByUserIdAndEventType(String userId, String eventType);

        /**
         * Count audit logs by user ID and service name
         */
        long countByUserIdAndServiceName(String userId, String serviceName);

        /**
         * Count audit logs by user ID and status
         */
        long countByUserIdAndStatus(String userId, AuditLogStatus status);

        /**
         * Count audit logs by service name and event type
         */
        long countByServiceNameAndEventType(String serviceName, String eventType);

        /**
         * Count audit logs by service name and status
         */
        long countByServiceNameAndStatus(String serviceName, AuditLogStatus status);

        /**
         * Count audit logs by event type and status
         */
        long countByEventTypeAndStatus(String eventType, AuditLogStatus status);

        /**
         * Count audit logs by resource and resource ID
         */
        long countByResourceAndResourceId(String resource, String resourceId);

        /**
         * Count audit logs by user ID and timestamp range
         */
        long countByUserIdAndTimestampBetween(String userId, LocalDateTime startDate, LocalDateTime endDate);

        /**
         * Count audit logs by service name and timestamp range
         */
        long countByServiceNameAndTimestampBetween(String serviceName, LocalDateTime startDate, LocalDateTime endDate);

        /**
         * Count audit logs by event type and timestamp range
         */
        long countByEventTypeAndTimestampBetween(String eventType, LocalDateTime startDate, LocalDateTime endDate);

        /**
         * Count audit logs by status and timestamp range
         */
        long countByStatusAndTimestampBetween(AuditLogStatus status, LocalDateTime startDate, LocalDateTime endDate);

        /**
         * Count audit logs with error messages
         */
        @Query(value = "{ 'errorMessage': { $exists: true, $ne: null } }", count = true)
        long countAuditLogsWithErrors();

        /**
         * Count audit logs by error code
         */
        long countByErrorCode(String errorCode);

        /**
         * Count audit logs by action
         */
        long countByAction(String action);

        /**
         * Count audit logs by resource ID
         */
        long countByResourceId(String resourceId);

        /**
         * Count audit logs by hash
         */
        long countByHash(String hash);

        /**
         * Count audit logs by previous hash
         */
        long countByPreviousHash(String previousHash);

        /**
         * Count audit logs by chain index
         */
        long countByChainIndex(Integer chainIndex);

        /**
         * Count audit logs by chain index range
         */
        long countByChainIndexBetween(Integer startIndex, Integer endIndex);

        /**
         * Count audit logs by chain ID and chain index range
         */
        long countByChainIdAndChainIndexBetween(String chainId, Integer startIndex, Integer endIndex);

        /**
         * Count audit logs by chain ID and user ID
         */
        long countByChainIdAndUserId(String chainId, String userId);

        /**
         * Count audit logs by chain ID and service name
         */
        long countByChainIdAndServiceName(String chainId, String serviceName);

        /**
         * Count audit logs by chain ID and event type
         */
        long countByChainIdAndEventType(String chainId, String eventType);

        /**
         * Count audit logs by chain ID and status
         */
        long countByChainIdAndStatus(String chainId, AuditLogStatus status);

        /**
         * Count audit logs by chain ID and timestamp range
         */
        long countByChainIdAndTimestampBetween(String chainId, LocalDateTime startDate, LocalDateTime endDate);
}
