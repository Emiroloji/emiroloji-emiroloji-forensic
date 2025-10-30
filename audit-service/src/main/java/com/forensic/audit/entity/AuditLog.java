package com.forensic.audit.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Audit Log entity for MongoDB
 * 
 * This entity represents an immutable audit log entry that cannot be modified
 * once created, ensuring forensic integrity and chain-of-custody.
 */
@Document(collection = "audit_logs")
public class AuditLog {

    @Id
    private String id;

    @NotBlank
    @Indexed
    private String eventId;

    @NotBlank
    @Indexed
    private String eventType;

    @NotBlank
    @Indexed
    private String serviceName;

    @NotBlank
    @Indexed
    private String userId;

    @Indexed
    private String sessionId;

    @NotBlank
    @Indexed
    private String action;

    @NotBlank
    private String resource;

    @Indexed
    private String resourceId;

    @NotNull
    @Indexed
    private LocalDateTime timestamp;

    @NotBlank
    private String ipAddress;

    private String userAgent;

    private String requestId;

    private String correlationId;

    @NotNull
    private AuditLogStatus status;

    private String errorMessage;

    private String errorCode;

    private Map<String, Object> metadata;

    private Map<String, Object> requestData;

    private Map<String, Object> responseData;

    @NotBlank
    private String hash;

    @Indexed
    private String previousHash;

    @Indexed
    private String chainId;

    @Indexed
    private Integer chainIndex;

    // Constructors
    public AuditLog() {
    }

    public AuditLog(String eventId, String eventType, String serviceName, String userId,
            String action, String resource, String resourceId, String ipAddress) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.serviceName = serviceName;
        this.userId = userId;
        this.action = action;
        this.resource = resource;
        this.resourceId = resourceId;
        this.ipAddress = ipAddress;
        this.timestamp = LocalDateTime.now();
        this.status = AuditLogStatus.SUCCESS;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

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

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
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

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public AuditLogStatus getStatus() {
        return status;
    }

    public void setStatus(AuditLogStatus status) {
        this.status = status;
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

    public Map<String, Object> getRequestData() {
        return requestData;
    }

    public void setRequestData(Map<String, Object> requestData) {
        this.requestData = requestData;
    }

    public Map<String, Object> getResponseData() {
        return responseData;
    }

    public void setResponseData(Map<String, Object> responseData) {
        this.responseData = responseData;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getPreviousHash() {
        return previousHash;
    }

    public void setPreviousHash(String previousHash) {
        this.previousHash = previousHash;
    }

    public String getChainId() {
        return chainId;
    }

    public void setChainId(String chainId) {
        this.chainId = chainId;
    }

    public Integer getChainIndex() {
        return chainIndex;
    }

    public void setChainIndex(Integer chainIndex) {
        this.chainIndex = chainIndex;
    }

    // Business methods
    public boolean isSuccess() {
        return status == AuditLogStatus.SUCCESS;
    }

    public boolean isFailure() {
        return status == AuditLogStatus.FAILURE;
    }

    public boolean isWarning() {
        return status == AuditLogStatus.WARNING;
    }

    public boolean hasError() {
        return errorMessage != null && !errorMessage.trim().isEmpty();
    }

    public boolean isChainStart() {
        return previousHash == null || previousHash.trim().isEmpty();
    }

    public boolean isChainContinuation() {
        return previousHash != null && !previousHash.trim().isEmpty();
    }
}
