package com.forensic.audit.service;

import com.forensic.audit.entity.AuditLog;
import com.forensic.audit.entity.AuditLogStatus;
import com.forensic.audit.repository.AuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Audit Service for managing audit logs and chain-of-custody
 * 
 * This service provides:
 * - Immutable audit logging
 * - Chain-of-custody tracking
 * - Hash chain verification
 * - Audit log querying
 * - Compliance reporting
 */
@Service
@Transactional
public class AuditService {

    private static final Logger logger = LoggerFactory.getLogger(AuditService.class);

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private HashService hashService;

    /**
     * Create a new audit log entry
     * 
     * @param eventType   Event type
     * @param serviceName Service name
     * @param userId      User ID
     * @param action      Action performed
     * @param resource    Resource accessed
     * @param resourceId  Resource ID
     * @param ipAddress   IP address
     * @param metadata    Additional metadata
     * @return Created audit log
     */
    public AuditLog createAuditLog(String eventType, String serviceName, String userId,
            String action, String resource, String resourceId,
            String ipAddress, Map<String, Object> metadata) {
        try {
            // Generate unique event ID
            String eventId = UUID.randomUUID().toString();

            // Create audit log entry
            AuditLog auditLog = new AuditLog(eventId, eventType, serviceName, userId,
                    action, resource, resourceId, ipAddress);

            // Set additional fields
            auditLog.setMetadata(metadata);
            auditLog.setStatus(AuditLogStatus.SUCCESS);

            // Generate enhanced hash chain with forensic data
            generateEnhancedHashChain(auditLog);

            // Save to database
            AuditLog savedAuditLog = auditLogRepository.save(auditLog);

            logger.info("Audit log created: {} - {} by user {}", eventType, action, userId);
            return savedAuditLog;

        } catch (Exception e) {
            logger.error("Failed to create audit log: {}", e.getMessage());
            throw new RuntimeException("Audit log creation failed", e);
        }
    }

    /**
     * Create forensic audit log entry for face comparison operations
     * Includes matchScore, decision, and biometric metrics in the hash chain
     * 
     * @param eventType        Event type
     * @param serviceName      Service name
     * @param userId           User ID
     * @param action           Action performed
     * @param resource         Resource accessed
     * @param resourceId       Resource ID
     * @param ipAddress        IP address
     * @param matchScore       Face comparison match score (forensically critical)
     * @param decision         Face comparison decision (forensically critical)
     * @param biometricMetrics Additional biometric metrics (FRR, FAR, etc.)
     * @param metadata         Additional metadata
     * @return Created audit log
     */
    public AuditLog createForensicFaceComparisonLog(String eventType, String serviceName, String userId,
            String action, String resource, String resourceId, String ipAddress,
            Double matchScore, String decision, Map<String, Object> biometricMetrics,
            Map<String, Object> metadata) {
        try {
            // Generate unique event ID
            String eventId = UUID.randomUUID().toString();

            // Create audit log entry
            AuditLog auditLog = new AuditLog(eventId, eventType, serviceName, userId,
                    action, resource, resourceId, ipAddress);

            // Enhanced metadata with forensic data
            Map<String, Object> enhancedMetadata = new HashMap<>(metadata != null ? metadata : new HashMap<>());

            // Add critical forensic data to metadata
            if (matchScore != null) {
                enhancedMetadata.put("forensic_match_score", matchScore);
            }
            if (decision != null) {
                enhancedMetadata.put("forensic_decision", decision);
            }
            if (biometricMetrics != null) {
                enhancedMetadata.put("biometric_metrics", biometricMetrics);
            }

            // Add forensic timestamps and integrity markers
            enhancedMetadata.put("forensic_timestamp_nanos", System.nanoTime());
            enhancedMetadata.put("forensic_event_uuid", eventId);
            enhancedMetadata.put("forensic_compliance_level", "ISO_IEC_30107_3_2017");

            auditLog.setMetadata(enhancedMetadata);
            auditLog.setStatus(AuditLogStatus.SUCCESS);

            // Generate enhanced hash chain that includes forensic data
            generateForensicHashChain(auditLog, matchScore, decision, biometricMetrics);

            // Save to database
            AuditLog savedAuditLog = auditLogRepository.save(auditLog);

            logger.info("Forensic face comparison audit log created: {} - {} by user {} (score: {}, decision: {})",
                    eventType, action, userId, matchScore, decision);
            return savedAuditLog;

        } catch (Exception e) {
            logger.error("Failed to create forensic audit log: {}", e.getMessage());
            throw new RuntimeException("Forensic audit log creation failed", e);
        }
    }

    /**
     * Create forensic audit log for batch face comparison operations
     * 
     * @param eventType    Event type
     * @param serviceName  Service name
     * @param userId       User ID
     * @param action       Action performed
     * @param resource     Resource accessed
     * @param batchId      Batch operation ID
     * @param ipAddress    IP address
     * @param batchResults List of comparison results with forensic data
     * @param metadata     Additional metadata
     * @return Created audit log
     */
    public AuditLog createForensicBatchComparisonLog(String eventType, String serviceName, String userId,
            String action, String resource, String batchId, String ipAddress,
            List<Map<String, Object>> batchResults, Map<String, Object> metadata) {
        try {
            // Generate unique event ID
            String eventId = UUID.randomUUID().toString();

            // Create audit log entry
            AuditLog auditLog = new AuditLog(eventId, eventType, serviceName, userId,
                    action, resource, batchId, ipAddress);

            // Enhanced metadata with batch forensic data
            Map<String, Object> enhancedMetadata = new HashMap<>(metadata != null ? metadata : new HashMap<>());

            // Add batch forensic statistics
            enhancedMetadata.put("batch_total_comparisons", batchResults.size());
            enhancedMetadata.put("batch_results", batchResults);

            // Calculate batch statistics
            long matchCount = batchResults.stream()
                    .mapToLong(result -> "MATCH".equals(result.get("decision")) ? 1 : 0)
                    .sum();
            double avgScore = batchResults.stream()
                    .mapToDouble(result -> result.get("match_score") != null ? (Double) result.get("match_score") : 0.0)
                    .average().orElse(0.0);

            enhancedMetadata.put("batch_match_count", matchCount);
            enhancedMetadata.put("batch_average_score", avgScore);
            enhancedMetadata.put("batch_match_rate", (double) matchCount / batchResults.size());

            // Add forensic integrity markers
            enhancedMetadata.put("forensic_batch_timestamp", System.nanoTime());
            enhancedMetadata.put("forensic_batch_uuid", eventId);
            enhancedMetadata.put("forensic_batch_hash", calculateBatchResultsHash(batchResults));

            auditLog.setMetadata(enhancedMetadata);
            auditLog.setStatus(AuditLogStatus.SUCCESS);

            // Generate enhanced hash chain for batch operation
            generateForensicBatchHashChain(auditLog, batchResults);

            // Save to database
            AuditLog savedAuditLog = auditLogRepository.save(auditLog);

            logger.info("Forensic batch comparison audit log created: {} - {} by user {} ({} comparisons, {} matches)",
                    eventType, action, userId, batchResults.size(), matchCount);
            return savedAuditLog;

        } catch (Exception e) {
            logger.error("Failed to create forensic batch audit log: {}", e.getMessage());
            throw new RuntimeException("Forensic batch audit log creation failed", e);
        }
    }

    /**
     * Create audit log with error information
     * 
     * @param eventType    Event type
     * @param serviceName  Service name
     * @param userId       User ID
     * @param action       Action attempted
     * @param resource     Resource accessed
     * @param resourceId   Resource ID
     * @param ipAddress    IP address
     * @param errorMessage Error message
     * @param errorCode    Error code
     * @param metadata     Additional metadata
     * @return Created audit log
     */
    public AuditLog createErrorAuditLog(String eventType, String serviceName, String userId,
            String action, String resource, String resourceId,
            String ipAddress, String errorMessage, String errorCode,
            Map<String, Object> metadata) {
        try {
            // Generate unique event ID
            String eventId = UUID.randomUUID().toString();

            // Create audit log entry
            AuditLog auditLog = new AuditLog(eventId, eventType, serviceName, userId,
                    action, resource, resourceId, ipAddress);

            // Set error information
            auditLog.setStatus(AuditLogStatus.FAILURE);
            auditLog.setErrorMessage(errorMessage);
            auditLog.setErrorCode(errorCode);
            auditLog.setMetadata(metadata);

            // Generate hash chain
            generateHashChain(auditLog);

            // Save to database
            AuditLog savedAuditLog = auditLogRepository.save(auditLog);

            logger.warn("Error audit log created: {} - {} by user {}: {}", eventType, action, userId, errorMessage);
            return savedAuditLog;

        } catch (Exception e) {
            logger.error("Failed to create error audit log: {}", e.getMessage());
            throw new RuntimeException("Error audit log creation failed", e);
        }
    }

    /**
     * Get audit log by ID
     * 
     * @param id Audit log ID
     * @return Audit log
     */
    @Transactional(readOnly = true)
    public Optional<AuditLog> getAuditLogById(String id) {
        try {
            return auditLogRepository.findById(id);
        } catch (Exception e) {
            logger.error("Failed to get audit log by ID {}: {}", id, e.getMessage());
            throw new RuntimeException("Failed to get audit log", e);
        }
    }

    /**
     * Get audit logs by user ID
     * 
     * @param userId   User ID
     * @param pageable Pagination parameters
     * @return Page of audit logs
     */
    @Transactional(readOnly = true)
    public Page<AuditLog> getAuditLogsByUserId(String userId, Pageable pageable) {
        try {
            return auditLogRepository.findByUserId(userId, pageable);
        } catch (Exception e) {
            logger.error("Failed to get audit logs by user ID {}: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to get audit logs", e);
        }
    }

    /**
     * Get audit logs by event type
     * 
     * @param eventType Event type
     * @param pageable  Pagination parameters
     * @return Page of audit logs
     */
    @Transactional(readOnly = true)
    public Page<AuditLog> getAuditLogsByEventType(String eventType, Pageable pageable) {
        try {
            return auditLogRepository.findByEventType(eventType, pageable);
        } catch (Exception e) {
            logger.error("Failed to get audit logs by event type {}: {}", eventType, e.getMessage());
            throw new RuntimeException("Failed to get audit logs", e);
        }
    }

    /**
     * Get audit logs by service name
     * 
     * @param serviceName Service name
     * @param pageable    Pagination parameters
     * @return Page of audit logs
     */
    @Transactional(readOnly = true)
    public Page<AuditLog> getAuditLogsByServiceName(String serviceName, Pageable pageable) {
        try {
            return auditLogRepository.findByServiceName(serviceName, pageable);
        } catch (Exception e) {
            logger.error("Failed to get audit logs by service name {}: {}", serviceName, e.getMessage());
            throw new RuntimeException("Failed to get audit logs", e);
        }
    }

    /**
     * Get audit logs by resource
     * 
     * @param resource Resource name
     * @param pageable Pagination parameters
     * @return Page of audit logs
     */
    @Transactional(readOnly = true)
    public Page<AuditLog> getAuditLogsByResource(String resource, Pageable pageable) {
        try {
            return auditLogRepository.findByResource(resource, pageable);
        } catch (Exception e) {
            logger.error("Failed to get audit logs by resource {}: {}", resource, e.getMessage());
            throw new RuntimeException("Failed to get audit logs", e);
        }
    }

    /**
     * Get audit logs by status
     * 
     * @param status   Audit log status
     * @param pageable Pagination parameters
     * @return Page of audit logs
     */
    @Transactional(readOnly = true)
    public Page<AuditLog> getAuditLogsByStatus(AuditLogStatus status, Pageable pageable) {
        try {
            return auditLogRepository.findByStatus(status, pageable);
        } catch (Exception e) {
            logger.error("Failed to get audit logs by status {}: {}", status, e.getMessage());
            throw new RuntimeException("Failed to get audit logs", e);
        }
    }

    /**
     * Get audit logs by date range
     * 
     * @param startDate Start date
     * @param endDate   End date
     * @param pageable  Pagination parameters
     * @return Page of audit logs
     */
    @Transactional(readOnly = true)
    public Page<AuditLog> getAuditLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        try {
            return auditLogRepository.findByTimestampBetween(startDate, endDate, pageable);
        } catch (Exception e) {
            logger.error("Failed to get audit logs by date range: {}", e.getMessage());
            throw new RuntimeException("Failed to get audit logs", e);
        }
    }

    /**
     * Get audit logs by chain ID
     * 
     * @param chainId  Chain ID
     * @param pageable Pagination parameters
     * @return Page of audit logs
     */
    @Transactional(readOnly = true)
    public Page<AuditLog> getAuditLogsByChainId(String chainId, Pageable pageable) {
        try {
            return auditLogRepository.findByChainId(chainId, pageable);
        } catch (Exception e) {
            logger.error("Failed to get audit logs by chain ID {}: {}", chainId, e.getMessage());
            throw new RuntimeException("Failed to get audit logs", e);
        }
    }

    /**
     * Search audit logs by multiple criteria
     * 
     * @param userId      User ID (optional)
     * @param eventType   Event type (optional)
     * @param serviceName Service name (optional)
     * @param resource    Resource (optional)
     * @param status      Status (optional)
     * @param startDate   Start date (optional)
     * @param endDate     End date (optional)
     * @param pageable    Pagination parameters
     * @return Page of audit logs
     */
    @Transactional(readOnly = true)
    public Page<AuditLog> searchAuditLogs(String userId, String eventType, String serviceName,
            String resource, AuditLogStatus status,
            LocalDateTime startDate, LocalDateTime endDate,
            Pageable pageable) {
        try {
            return auditLogRepository.findByMultipleCriteria(userId, eventType, serviceName,
                    resource, status, startDate, endDate, pageable);
        } catch (Exception e) {
            logger.error("Failed to search audit logs: {}", e.getMessage());
            throw new RuntimeException("Audit log search failed", e);
        }
    }

    /**
     * Get audit log statistics
     * 
     * @return Audit log statistics
     */
    @Transactional(readOnly = true)
    public AuditLogStatistics getAuditLogStatistics() {
        try {
            long totalLogs = auditLogRepository.count();
            long successLogs = auditLogRepository.countByStatus(AuditLogStatus.SUCCESS);
            long failureLogs = auditLogRepository.countByStatus(AuditLogStatus.FAILURE);
            long warningLogs = auditLogRepository.countByStatus(AuditLogStatus.WARNING);
            long infoLogs = auditLogRepository.countByStatus(AuditLogStatus.INFO);

            return new AuditLogStatistics(totalLogs, successLogs, failureLogs, warningLogs, infoLogs);

        } catch (Exception e) {
            logger.error("Failed to get audit log statistics: {}", e.getMessage());
            throw new RuntimeException("Failed to get audit log statistics", e);
        }
    }

    /**
     * Verify hash chain integrity
     * 
     * @param chainId Chain ID to verify
     * @return True if chain is intact, False otherwise
     */
    @Transactional(readOnly = true)
    public boolean verifyHashChain(String chainId) {
        try {
            List<AuditLog> chainLogs = auditLogRepository.findByChainIdOrderByChainIndex(chainId);

            if (chainLogs.isEmpty()) {
                return false;
            }

            // Verify each link in the chain
            for (int i = 0; i < chainLogs.size(); i++) {
                AuditLog currentLog = chainLogs.get(i);

                // Calculate expected hash
                String expectedHash = hashService.calculateHash(currentLog);

                // Verify hash matches
                if (!expectedHash.equals(currentLog.getHash())) {
                    logger.error("Hash mismatch in chain {} at index {}", chainId, i);
                    return false;
                }

                // Verify previous hash (except for first entry)
                if (i > 0) {
                    AuditLog previousLog = chainLogs.get(i - 1);
                    if (!previousLog.getHash().equals(currentLog.getPreviousHash())) {
                        logger.error("Previous hash mismatch in chain {} at index {}", chainId, i);
                        return false;
                    }
                }
            }

            logger.info("Hash chain {} verified successfully", chainId);
            return true;

        } catch (Exception e) {
            logger.error("Failed to verify hash chain {}: {}", chainId, e.getMessage());
            return false;
        }
    }

    /**
     * Generate hash chain for audit log
     * 
     * @param auditLog Audit log to process
     */
    private void generateHashChain(AuditLog auditLog) {
        try {
            // Get the last audit log in the chain
            Optional<AuditLog> lastLog = auditLogRepository.findTopByOrderByChainIndexDesc();

            if (lastLog.isPresent()) {
                // Continue existing chain
                auditLog.setPreviousHash(lastLog.get().getHash());
                auditLog.setChainId(lastLog.get().getChainId());
                auditLog.setChainIndex(lastLog.get().getChainIndex() + 1);
            } else {
                // Start new chain
                auditLog.setPreviousHash(null);
                auditLog.setChainId(UUID.randomUUID().toString());
                auditLog.setChainIndex(0);
            }

            // Calculate hash for current log
            String hash = hashService.calculateHash(auditLog);
            auditLog.setHash(hash);

        } catch (Exception e) {
            logger.error("Failed to generate hash chain: {}", e.getMessage());
            throw new RuntimeException("Hash chain generation failed", e);
        }
    }

    /**
     * Generate enhanced hash chain for audit log with additional forensic integrity
     * 
     * @param auditLog Audit log to process
     */
    private void generateEnhancedHashChain(AuditLog auditLog) {
        try {
            // Get the last audit log in the chain
            Optional<AuditLog> lastLog = auditLogRepository.findTopByOrderByChainIndexDesc();

            if (lastLog.isPresent()) {
                // Continue existing chain
                auditLog.setPreviousHash(lastLog.get().getHash());
                auditLog.setChainId(lastLog.get().getChainId());
                auditLog.setChainIndex(lastLog.get().getChainIndex() + 1);
            } else {
                // Start new chain with forensic markers
                auditLog.setPreviousHash(null);
                auditLog.setChainId(UUID.randomUUID().toString());
                auditLog.setChainIndex(0);
            }

            // Calculate enhanced hash including metadata
            String hash = hashService.calculateEnhancedHash(auditLog);
            auditLog.setHash(hash);

        } catch (Exception e) {
            logger.error("Failed to generate enhanced hash chain: {}", e.getMessage());
            throw new RuntimeException("Enhanced hash chain generation failed", e);
        }
    }

    /**
     * Generate forensic hash chain for face comparison operations
     * Includes matchScore and decision in the hash calculation for immutability
     * 
     * @param auditLog         Audit log to process
     * @param matchScore       Face comparison match score
     * @param decision         Face comparison decision
     * @param biometricMetrics Additional biometric metrics
     */
    private void generateForensicHashChain(AuditLog auditLog, Double matchScore, String decision,
            Map<String, Object> biometricMetrics) {
        try {
            // Get the last audit log in the chain
            Optional<AuditLog> lastLog = auditLogRepository.findTopByOrderByChainIndexDesc();

            if (lastLog.isPresent()) {
                // Continue existing chain
                auditLog.setPreviousHash(lastLog.get().getHash());
                auditLog.setChainId(lastLog.get().getChainId());
                auditLog.setChainIndex(lastLog.get().getChainIndex() + 1);
            } else {
                // Start new forensic chain
                auditLog.setPreviousHash(null);
                auditLog.setChainId("FORENSIC_" + UUID.randomUUID().toString());
                auditLog.setChainIndex(0);
            }

            // Calculate forensic hash that includes critical biometric data
            String hash = hashService.calculateForensicHash(auditLog, matchScore, decision, biometricMetrics);
            auditLog.setHash(hash);

        } catch (Exception e) {
            logger.error("Failed to generate forensic hash chain: {}", e.getMessage());
            throw new RuntimeException("Forensic hash chain generation failed", e);
        }
    }

    /**
     * Generate forensic hash chain for batch operations
     * 
     * @param auditLog     Audit log to process
     * @param batchResults Batch comparison results
     */
    private void generateForensicBatchHashChain(AuditLog auditLog, List<Map<String, Object>> batchResults) {
        try {
            // Get the last audit log in the chain
            Optional<AuditLog> lastLog = auditLogRepository.findTopByOrderByChainIndexDesc();

            if (lastLog.isPresent()) {
                // Continue existing chain
                auditLog.setPreviousHash(lastLog.get().getHash());
                auditLog.setChainId(lastLog.get().getChainId());
                auditLog.setChainIndex(lastLog.get().getChainIndex() + 1);
            } else {
                // Start new forensic batch chain
                auditLog.setPreviousHash(null);
                auditLog.setChainId("FORENSIC_BATCH_" + UUID.randomUUID().toString());
                auditLog.setChainIndex(0);
            }

            // Calculate batch forensic hash
            String hash = hashService.calculateBatchForensicHash(auditLog, batchResults);
            auditLog.setHash(hash);

        } catch (Exception e) {
            logger.error("Failed to generate forensic batch hash chain: {}", e.getMessage());
            throw new RuntimeException("Forensic batch hash chain generation failed", e);
        }
    }

    /**
     * Calculate hash for batch results to ensure integrity
     * 
     * @param batchResults List of batch comparison results
     * @return Hash of batch results
     */
    private String calculateBatchResultsHash(List<Map<String, Object>> batchResults) {
        try {
            StringBuilder dataToHash = new StringBuilder();

            // Sort results by ID for consistent hashing
            batchResults.stream()
                    .sorted((r1, r2) -> String.valueOf(r1.get("comparison_id"))
                            .compareTo(String.valueOf(r2.get("comparison_id"))))
                    .forEach(result -> {
                        dataToHash.append("RESULT|");
                        dataToHash.append(result.get("comparison_id")).append("|");
                        dataToHash.append(result.get("match_score")).append("|");
                        dataToHash.append(result.get("decision")).append("|");
                        if (result.get("frr") != null) {
                            dataToHash.append("FRR:").append(result.get("frr")).append("|");
                        }
                        if (result.get("far") != null) {
                            dataToHash.append("FAR:").append(result.get("far")).append("|");
                        }
                    });

            // Calculate SHA-256 hash
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(dataToHash.toString().getBytes());

            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            logger.error("Failed to calculate batch results hash: {}", e.getMessage());
            throw new RuntimeException("Hash calculation failed", e);
        }
    }

    /**
     * Verify forensic hash chain integrity with enhanced validation
     * Includes validation of critical forensic data (matchScore, decision)
     * 
     * @param chainId Chain ID to verify
     * @return Forensic verification result with detailed information
     */
    @SuppressWarnings("unchecked")
    public ForensicVerificationResult verifyForensicHashChain(String chainId) {
        try {
            List<AuditLog> chainLogs = auditLogRepository.findByChainIdOrderByChainIndex(chainId);

            if (chainLogs.isEmpty()) {
                return new ForensicVerificationResult(false, "Chain not found", chainId, 0);
            }

            int verifiedLinks = 0;
            List<String> verificationIssues = new ArrayList<>();

            // Verify each link in the chain with forensic validation
            for (int i = 0; i < chainLogs.size(); i++) {
                AuditLog currentLog = chainLogs.get(i);

                // Calculate expected hash using appropriate method
                String expectedHash;
                if (currentLog.getMetadata() != null && currentLog.getMetadata().containsKey("forensic_match_score")) {
                    // Use forensic hash calculation for face comparison logs
                    expectedHash = hashService.calculateForensicHash(currentLog,
                            (Double) currentLog.getMetadata().get("forensic_match_score"),
                            (String) currentLog.getMetadata().get("forensic_decision"),
                            (Map<String, Object>) currentLog.getMetadata().get("biometric_metrics"));
                } else if (currentLog.getMetadata() != null && currentLog.getMetadata().containsKey("batch_results")) {
                    // Use batch forensic hash calculation
                    expectedHash = hashService.calculateBatchForensicHash(currentLog,
                            (List<Map<String, Object>>) currentLog.getMetadata().get("batch_results"));
                } else {
                    // Use standard hash calculation
                    expectedHash = hashService.calculateHash(currentLog);
                }

                // Verify hash matches
                if (!expectedHash.equals(currentLog.getHash())) {
                    verificationIssues.add("Hash mismatch at index " + i + ": expected " + expectedHash +
                            ", found " + currentLog.getHash());
                    continue;
                }

                // Verify previous hash (except for first entry)
                if (i > 0) {
                    AuditLog previousLog = chainLogs.get(i - 1);
                    if (!previousLog.getHash().equals(currentLog.getPreviousHash())) {
                        verificationIssues.add("Previous hash mismatch at index " + i);
                        continue;
                    }
                }

                // Verify forensic-specific data integrity
                if (currentLog.getMetadata() != null) {
                    if (!verifyForensicDataIntegrity(currentLog)) {
                        verificationIssues.add("Forensic data integrity issue at index " + i);
                        continue;
                    }
                }

                verifiedLinks++;
            }

            boolean isValid = verificationIssues.isEmpty();
            String message = isValid ? "Forensic chain verified successfully"
                    : "Forensic chain verification failed: " + String.join("; ", verificationIssues);

            logger.info("Forensic hash chain {} verification result: {} ({}/{} links verified)",
                    chainId, isValid, verifiedLinks, chainLogs.size());

            return new ForensicVerificationResult(isValid, message, chainId, verifiedLinks);

        } catch (Exception e) {
            logger.error("Failed to verify forensic hash chain {}: {}", chainId, e.getMessage());
            return new ForensicVerificationResult(false, "Verification error: " + e.getMessage(), chainId, 0);
        }
    }

    /**
     * Verify integrity of forensic-specific data within audit log
     * 
     * @param auditLog Audit log to verify
     * @return True if forensic data is intact
     */
    @SuppressWarnings("unchecked")
    private boolean verifyForensicDataIntegrity(AuditLog auditLog) {
        try {
            Map<String, Object> metadata = auditLog.getMetadata();
            if (metadata == null) {
                return true; // No forensic data to verify
            }

            // Verify forensic match score is within valid range
            if (metadata.containsKey("forensic_match_score")) {
                Double matchScore = (Double) metadata.get("forensic_match_score");
                if (matchScore == null || matchScore < 0.0 || matchScore > 1.0) {
                    logger.warn("Invalid forensic match score: {}", matchScore);
                    return false;
                }
            }

            // Verify forensic decision is valid
            if (metadata.containsKey("forensic_decision")) {
                String decision = (String) metadata.get("forensic_decision");
                if (decision == null || (!decision.equals("MATCH") && !decision.equals("NO_MATCH")
                        && !decision.equals("UNCERTAIN"))) {
                    logger.warn("Invalid forensic decision: {}", decision);
                    return false;
                }
            }

            // Verify biometric metrics are present and valid
            if (metadata.containsKey("biometric_metrics")) {
                Map<String, Object> biometrics = (Map<String, Object>) metadata.get("biometric_metrics");
                if (biometrics != null) {
                    // Verify FRR and FAR are within valid ranges
                    if (biometrics.containsKey("false_reject_rate")) {
                        Double frr = (Double) biometrics.get("false_reject_rate");
                        if (frr == null || frr < 0.0 || frr > 1.0) {
                            logger.warn("Invalid FRR value: {}", frr);
                            return false;
                        }
                    }
                    if (biometrics.containsKey("false_accept_rate")) {
                        Double far = (Double) biometrics.get("false_accept_rate");
                        if (far == null || far < 0.0 || far > 1.0) {
                            logger.warn("Invalid FAR value: {}", far);
                            return false;
                        }
                    }
                }
            }

            return true;

        } catch (Exception e) {
            logger.error("Error verifying forensic data integrity: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Forensic verification result class
     */
    public static class ForensicVerificationResult {
        private final boolean isValid;
        private final String message;
        private final String chainId;
        private final int verifiedLinks;

        public ForensicVerificationResult(boolean isValid, String message, String chainId, int verifiedLinks) {
            this.isValid = isValid;
            this.message = message;
            this.chainId = chainId;
            this.verifiedLinks = verifiedLinks;
        }

        // Getters
        public boolean isValid() {
            return isValid;
        }

        public String getMessage() {
            return message;
        }

        public String getChainId() {
            return chainId;
        }

        public int getVerifiedLinks() {
            return verifiedLinks;
        }
    }

    /**
     * Audit log statistics data class
     */
    public static class AuditLogStatistics {
        private final long totalLogs;
        private final long successLogs;
        private final long failureLogs;
        private final long warningLogs;
        private final long infoLogs;

        public AuditLogStatistics(long totalLogs, long successLogs, long failureLogs,
                long warningLogs, long infoLogs) {
            this.totalLogs = totalLogs;
            this.successLogs = successLogs;
            this.failureLogs = failureLogs;
            this.warningLogs = warningLogs;
            this.infoLogs = infoLogs;
        }

        // Getters
        public long getTotalLogs() {
            return totalLogs;
        }

        public long getSuccessLogs() {
            return successLogs;
        }

        public long getFailureLogs() {
            return failureLogs;
        }

        public long getWarningLogs() {
            return warningLogs;
        }

        public long getInfoLogs() {
            return infoLogs;
        }
    }
}
