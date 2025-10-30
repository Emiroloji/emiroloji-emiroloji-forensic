package com.forensic.audit.controller;

import com.forensic.audit.entity.AuditLog;
import com.forensic.audit.service.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.forensic.audit.entity.AuditLogStatus;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

/**
 * Audit Controller
 * 
 * Handles audit logging and chain-of-custody endpoints
 */
@RestController
@RequestMapping("/api/audit")
@Tag(name = "Audit Management", description = "Audit logging and chain-of-custody APIs")
public class AuditController {

    private static final Logger logger = LoggerFactory.getLogger(AuditController.class);

    @Autowired
    private AuditService auditService;

    /**
     * Create audit log entry
     */
    @PostMapping("/log")
    @Operation(summary = "Create audit log", description = "Create a new audit log entry")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVESTIGATOR', 'ANALYST')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> createAuditLog(@RequestBody AuditLogRequest request) {
        try {
            logger.info("Creating audit log: {} - {} by user {}", request.getEventType(), request.getAction(),
                    request.getUserId());

            AuditLog auditLog = auditService.createAuditLog(
                    request.getEventType(),
                    request.getServiceName(),
                    request.getUserId(),
                    request.getAction(),
                    request.getResource(),
                    request.getResourceId(),
                    request.getIpAddress(),
                    request.getMetadata());

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(auditLog);

        } catch (Exception e) {
            logger.error("Failed to create audit log: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body("Failed to create audit log: " + e.getMessage());
        }
    }

    /**
     * Create error audit log entry
     */
    @PostMapping("/log/error")
    @Operation(summary = "Create error audit log", description = "Create a new error audit log entry")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVESTIGATOR', 'ANALYST')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> createErrorAuditLog(@RequestBody ErrorAuditLogRequest request) {
        try {
            logger.warn("Creating error audit log: {} - {} by user {}: {}",
                    request.getEventType(), request.getAction(), request.getUserId(), request.getErrorMessage());

            AuditLog auditLog = auditService.createErrorAuditLog(
                    request.getEventType(),
                    request.getServiceName(),
                    request.getUserId(),
                    request.getAction(),
                    request.getResource(),
                    request.getResourceId(),
                    request.getIpAddress(),
                    request.getErrorMessage(),
                    request.getErrorCode(),
                    request.getMetadata());

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(auditLog);

        } catch (Exception e) {
            logger.error("Failed to create error audit log: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body("Failed to create error audit log: " + e.getMessage());
        }
    }

    /**
     * Get audit log by ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get audit log by ID", description = "Get an audit log by its ID")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVESTIGATOR', 'ANALYST', 'VIEWER')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> getAuditLogById(@PathVariable String id) {
        try {
            Optional<AuditLog> auditLog = auditService.getAuditLogById(id);

            if (auditLog.isPresent()) {
                return ResponseEntity.ok(auditLog.get());
            } else {
                return ResponseEntity.notFound().build();
            }

        } catch (Exception e) {
            logger.error("Failed to get audit log {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest()
                    .body("Failed to get audit log: " + e.getMessage());
        }
    }

    /**
     * Get audit logs by user ID
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "Get audit logs by user", description = "Get audit logs for a specific user")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVESTIGATOR', 'ANALYST', 'VIEWER')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> getAuditLogsByUserId(@PathVariable String userId, Pageable pageable) {
        try {
            Page<AuditLog> auditLogs = auditService.getAuditLogsByUserId(userId, pageable);
            return ResponseEntity.ok(auditLogs);

        } catch (Exception e) {
            logger.error("Failed to get audit logs for user {}: {}", userId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body("Failed to get audit logs: " + e.getMessage());
        }
    }

    /**
     * Get audit logs by event type
     */
    @GetMapping("/event-type/{eventType}")
    @Operation(summary = "Get audit logs by event type", description = "Get audit logs for a specific event type")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVESTIGATOR', 'ANALYST', 'VIEWER')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> getAuditLogsByEventType(@PathVariable String eventType, Pageable pageable) {
        try {
            Page<AuditLog> auditLogs = auditService.getAuditLogsByEventType(eventType, pageable);
            return ResponseEntity.ok(auditLogs);

        } catch (Exception e) {
            logger.error("Failed to get audit logs for event type {}: {}", eventType, e.getMessage());
            return ResponseEntity.badRequest()
                    .body("Failed to get audit logs: " + e.getMessage());
        }
    }

    /**
     * Get audit logs by service name
     */
    @GetMapping("/service/{serviceName}")
    @Operation(summary = "Get audit logs by service", description = "Get audit logs for a specific service")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVESTIGATOR', 'ANALYST', 'VIEWER')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> getAuditLogsByServiceName(@PathVariable String serviceName, Pageable pageable) {
        try {
            Page<AuditLog> auditLogs = auditService.getAuditLogsByServiceName(serviceName, pageable);
            return ResponseEntity.ok(auditLogs);

        } catch (Exception e) {
            logger.error("Failed to get audit logs for service {}: {}", serviceName, e.getMessage());
            return ResponseEntity.badRequest()
                    .body("Failed to get audit logs: " + e.getMessage());
        }
    }

    /**
     * Get audit logs by resource
     */
    @GetMapping("/resource/{resource}")
    @Operation(summary = "Get audit logs by resource", description = "Get audit logs for a specific resource")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVESTIGATOR', 'ANALYST', 'VIEWER')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> getAuditLogsByResource(@PathVariable String resource, Pageable pageable) {
        try {
            Page<AuditLog> auditLogs = auditService.getAuditLogsByResource(resource, pageable);
            return ResponseEntity.ok(auditLogs);

        } catch (Exception e) {
            logger.error("Failed to get audit logs for resource {}: {}", resource, e.getMessage());
            return ResponseEntity.badRequest()
                    .body("Failed to get audit logs: " + e.getMessage());
        }
    }

    /**
     * Get audit logs by status
     */
    @GetMapping("/status/{status}")
    @Operation(summary = "Get audit logs by status", description = "Get audit logs with a specific status")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVESTIGATOR', 'ANALYST', 'VIEWER')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> getAuditLogsByStatus(@PathVariable AuditLogStatus status, Pageable pageable) {
        try {
            Page<AuditLog> auditLogs = auditService.getAuditLogsByStatus(status, pageable);
            return ResponseEntity.ok(auditLogs);

        } catch (Exception e) {
            logger.error("Failed to get audit logs for status {}: {}", status, e.getMessage());
            return ResponseEntity.badRequest()
                    .body("Failed to get audit logs: " + e.getMessage());
        }
    }

    /**
     * Get audit logs by date range
     */
    @GetMapping("/date-range")
    @Operation(summary = "Get audit logs by date range", description = "Get audit logs within a date range")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVESTIGATOR', 'ANALYST', 'VIEWER')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> getAuditLogsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            Pageable pageable) {
        try {
            Page<AuditLog> auditLogs = auditService.getAuditLogsByDateRange(startDate, endDate, pageable);
            return ResponseEntity.ok(auditLogs);

        } catch (Exception e) {
            logger.error("Failed to get audit logs by date range: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body("Failed to get audit logs: " + e.getMessage());
        }
    }

    /**
     * Get audit logs by chain ID
     */
    @GetMapping("/chain/{chainId}")
    @Operation(summary = "Get audit logs by chain ID", description = "Get audit logs for a specific chain")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVESTIGATOR', 'ANALYST', 'VIEWER')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> getAuditLogsByChainId(@PathVariable String chainId, Pageable pageable) {
        try {
            Page<AuditLog> auditLogs = auditService.getAuditLogsByChainId(chainId, pageable);
            return ResponseEntity.ok(auditLogs);

        } catch (Exception e) {
            logger.error("Failed to get audit logs for chain {}: {}", chainId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body("Failed to get audit logs: " + e.getMessage());
        }
    }

    /**
     * Search audit logs
     */
    @GetMapping("/search")
    @Operation(summary = "Search audit logs", description = "Search audit logs by multiple criteria")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVESTIGATOR', 'ANALYST', 'VIEWER')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> searchAuditLogs(
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String eventType,
            @RequestParam(required = false) String serviceName,
            @RequestParam(required = false) String resource,
            @RequestParam(required = false) AuditLogStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            Pageable pageable) {
        try {
            Page<AuditLog> auditLogs = auditService.searchAuditLogs(
                    userId, eventType, serviceName, resource, status, startDate, endDate, pageable);
            return ResponseEntity.ok(auditLogs);

        } catch (Exception e) {
            logger.error("Failed to search audit logs: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body("Failed to search audit logs: " + e.getMessage());
        }
    }

    /**
     * Get audit log statistics
     */
    @GetMapping("/statistics")
    @Operation(summary = "Get audit log statistics", description = "Get overall audit log statistics")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVESTIGATOR', 'ANALYST', 'VIEWER')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> getAuditLogStatistics() {
        try {
            AuditService.AuditLogStatistics statistics = auditService.getAuditLogStatistics();
            return ResponseEntity.ok(statistics);

        } catch (Exception e) {
            logger.error("Failed to get audit log statistics: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body("Failed to get audit log statistics: " + e.getMessage());
        }
    }

    /**
     * Verify hash chain integrity
     */
    @PostMapping("/verify-chain/{chainId}")
    @Operation(summary = "Verify hash chain", description = "Verify the integrity of a hash chain")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVESTIGATOR', 'ANALYST')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> verifyHashChain(@PathVariable String chainId) {
        try {
            boolean isIntact = auditService.verifyHashChain(chainId);

            return ResponseEntity.ok(Map.of(
                    "chainId", chainId,
                    "isIntact", isIntact,
                    "message", isIntact ? "Chain is intact" : "Chain integrity compromised"));

        } catch (Exception e) {
            logger.error("Failed to verify hash chain {}: {}", chainId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body("Failed to verify hash chain: " + e.getMessage());
        }
    }

    /**
     * Audit log request DTO
     */
    public static class AuditLogRequest {
        private String eventType;
        private String serviceName;
        private String userId;
        private String action;
        private String resource;
        private String resourceId;
        private String ipAddress;
        private Map<String, Object> metadata;

        // Getters and Setters
        public String getEventType() {
            return eventType;
        }

        public void setEventType(String eventType) {
            this.eventType = eventType;
        }

        public String getServiceName() {
            return serviceName;
        }

        public void setServiceName(String serviceName) {
            this.serviceName = serviceName;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getAction() {
            return action;
        }

        public void setAction(String action) {
            this.action = action;
        }

        public String getResource() {
            return resource;
        }

        public void setResource(String resource) {
            this.resource = resource;
        }

        public String getResourceId() {
            return resourceId;
        }

        public void setResourceId(String resourceId) {
            this.resourceId = resourceId;
        }

        public String getIpAddress() {
            return ipAddress;
        }

        public void setIpAddress(String ipAddress) {
            this.ipAddress = ipAddress;
        }

        public Map<String, Object> getMetadata() {
            return metadata;
        }

        public void setMetadata(Map<String, Object> metadata) {
            this.metadata = metadata;
        }
    }

    /**
     * Error audit log request DTO
     */
    public static class ErrorAuditLogRequest {
        private String eventType;
        private String serviceName;
        private String userId;
        private String action;
        private String resource;
        private String resourceId;
        private String ipAddress;
        private String errorMessage;
        private String errorCode;
        private Map<String, Object> metadata;

        // Getters and Setters
        public String getEventType() {
            return eventType;
        }

        public void setEventType(String eventType) {
            this.eventType = eventType;
        }

        public String getServiceName() {
            return serviceName;
        }

        public void setServiceName(String serviceName) {
            this.serviceName = serviceName;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getAction() {
            return action;
        }

        public void setAction(String action) {
            this.action = action;
        }

        public String getResource() {
            return resource;
        }

        public void setResource(String resource) {
            this.resource = resource;
        }

        public String getResourceId() {
            return resourceId;
        }

        public void setResourceId(String resourceId) {
            this.resourceId = resourceId;
        }

        public String getIpAddress() {
            return ipAddress;
        }

        public void setIpAddress(String ipAddress) {
            this.ipAddress = ipAddress;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }

        public String getErrorCode() {
            return errorCode;
        }

        public void setErrorCode(String errorCode) {
            this.errorCode = errorCode;
        }

        public Map<String, Object> getMetadata() {
            return metadata;
        }

        public void setMetadata(Map<String, Object> metadata) {
            this.metadata = metadata;
        }
    }
}
