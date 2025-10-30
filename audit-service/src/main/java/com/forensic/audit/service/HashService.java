package com.forensic.audit.service;

import com.forensic.audit.entity.AuditLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * Hash Service for calculating and verifying audit log hashes
 * 
 * This service provides:
 * - SHA-256 hash calculation
 * - Hash chain verification
 * - Tamper detection
 * - Integrity validation
 */
@Service
public class HashService {

    private static final Logger logger = LoggerFactory.getLogger(HashService.class);

    private static final String HASH_ALGORITHM = "SHA-256";

    /**
     * Calculate hash for audit log entry
     * 
     * @param auditLog Audit log entry
     * @return SHA-256 hash as Base64 string
     */
    public String calculateHash(AuditLog auditLog) {
        try {
            // Create hash input string
            StringBuilder hashInput = new StringBuilder();
            hashInput.append(auditLog.getEventId());
            hashInput.append(auditLog.getEventType());
            hashInput.append(auditLog.getServiceName());
            hashInput.append(auditLog.getUserId());
            hashInput.append(auditLog.getAction());
            hashInput.append(auditLog.getResource());
            hashInput.append(auditLog.getResourceId());
            hashInput.append(auditLog.getTimestamp().toString());
            hashInput.append(auditLog.getIpAddress());
            hashInput.append(auditLog.getStatus().name());

            // Add previous hash if exists
            if (auditLog.getPreviousHash() != null) {
                hashInput.append(auditLog.getPreviousHash());
            }

            // Add metadata hash if exists
            if (auditLog.getMetadata() != null) {
                hashInput.append(auditLog.getMetadata().toString());
            }

            // Calculate SHA-256 hash
            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
            byte[] hashBytes = digest.digest(hashInput.toString().getBytes(StandardCharsets.UTF_8));

            // Convert to Base64 string
            String hash = Base64.getEncoder().encodeToString(hashBytes);

            logger.debug("Hash calculated for audit log: {}", auditLog.getEventId());
            return hash;

        } catch (NoSuchAlgorithmException e) {
            logger.error("SHA-256 algorithm not available: {}", e.getMessage());
            throw new RuntimeException("Hash calculation failed", e);
        } catch (Exception e) {
            logger.error("Failed to calculate hash for audit log {}: {}", auditLog.getEventId(), e.getMessage());
            throw new RuntimeException("Hash calculation failed", e);
        }
    }

    /**
     * Calculate hash for arbitrary data
     * 
     * @param data Data to hash
     * @return SHA-256 hash as Base64 string
     */
    public String calculateHash(String data) {
        try {
            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
            byte[] hashBytes = digest.digest(data.getBytes(StandardCharsets.UTF_8));

            return Base64.getEncoder().encodeToString(hashBytes);

        } catch (NoSuchAlgorithmException e) {
            logger.error("SHA-256 algorithm not available: {}", e.getMessage());
            throw new RuntimeException("Hash calculation failed", e);
        } catch (Exception e) {
            logger.error("Failed to calculate hash for data: {}", e.getMessage());
            throw new RuntimeException("Hash calculation failed", e);
        }
    }

    /**
     * Calculate hash for byte array
     * 
     * @param data Data to hash
     * @return SHA-256 hash as Base64 string
     */
    public String calculateHash(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
            byte[] hashBytes = digest.digest(data);

            return Base64.getEncoder().encodeToString(hashBytes);

        } catch (NoSuchAlgorithmException e) {
            logger.error("SHA-256 algorithm not available: {}", e.getMessage());
            throw new RuntimeException("Hash calculation failed", e);
        } catch (Exception e) {
            logger.error("Failed to calculate hash for byte array: {}", e.getMessage());
            throw new RuntimeException("Hash calculation failed", e);
        }
    }

    /**
     * Verify hash for audit log entry
     * 
     * @param auditLog Audit log entry
     * @return True if hash is valid, False otherwise
     */
    public boolean verifyHash(AuditLog auditLog) {
        try {
            String expectedHash = calculateHash(auditLog);
            String actualHash = auditLog.getHash();

            boolean isValid = expectedHash.equals(actualHash);

            if (!isValid) {
                logger.warn("Hash verification failed for audit log: {}", auditLog.getEventId());
            }

            return isValid;

        } catch (Exception e) {
            logger.error("Failed to verify hash for audit log {}: {}", auditLog.getEventId(), e.getMessage());
            return false;
        }
    }

    /**
     * Verify hash chain integrity
     * 
     * @param auditLogs List of audit logs in chain order
     * @return True if chain is intact, False otherwise
     */
    public boolean verifyHashChain(java.util.List<AuditLog> auditLogs) {
        try {
            if (auditLogs.isEmpty()) {
                return false;
            }

            // Verify each log's hash
            for (AuditLog auditLog : auditLogs) {
                if (!verifyHash(auditLog)) {
                    logger.error("Hash verification failed for audit log: {}", auditLog.getEventId());
                    return false;
                }
            }

            // Verify chain links
            for (int i = 1; i < auditLogs.size(); i++) {
                AuditLog currentLog = auditLogs.get(i);
                AuditLog previousLog = auditLogs.get(i - 1);

                if (!previousLog.getHash().equals(currentLog.getPreviousHash())) {
                    logger.error("Chain link verification failed between logs: {} and {}",
                            previousLog.getEventId(), currentLog.getEventId());
                    return false;
                }
            }

            logger.info("Hash chain verification successful for {} logs", auditLogs.size());
            return true;

        } catch (Exception e) {
            logger.error("Failed to verify hash chain: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Generate hash chain summary
     * 
     * @param auditLogs List of audit logs in chain order
     * @return Hash chain summary
     */
    public HashChainSummary generateHashChainSummary(java.util.List<AuditLog> auditLogs) {
        try {
            if (auditLogs.isEmpty()) {
                return new HashChainSummary(null, 0, false, "Empty chain");
            }

            String chainId = auditLogs.get(0).getChainId();
            int chainLength = auditLogs.size();
            boolean isIntact = verifyHashChain(auditLogs);

            String summary = String.format("Chain ID: %s, Length: %d, Intact: %s",
                    chainId, chainLength, isIntact);

            return new HashChainSummary(chainId, chainLength, isIntact, summary);

        } catch (Exception e) {
            logger.error("Failed to generate hash chain summary: {}", e.getMessage());
            return new HashChainSummary(null, 0, false, "Error generating summary");
        }
    }

    /**
     * Hash chain summary data class
     */
    public static class HashChainSummary {
        private final String chainId;
        private final int chainLength;
        private final boolean isIntact;
        private final String summary;

        public HashChainSummary(String chainId, int chainLength, boolean isIntact, String summary) {
            this.chainId = chainId;
            this.chainLength = chainLength;
            this.isIntact = isIntact;
            this.summary = summary;
        }

        // Getters
        public String getChainId() {
            return chainId;
        }

        public int getChainLength() {
            return chainLength;
        }

        public boolean isIntact() {
            return isIntact;
        }

        public String getSummary() {
            return summary;
        }
    }

    /**
     * Calculate enhanced hash for audit log with additional metadata integrity
     * 
     * @param auditLog Audit log entry
     * @return Enhanced SHA-256 hash as Base64 string
     */
    public String calculateEnhancedHash(AuditLog auditLog) {
        try {
            // Create enhanced hash input string with all standard fields
            StringBuilder hashInput = new StringBuilder();
            hashInput.append(auditLog.getEventId());
            hashInput.append(auditLog.getEventType());
            hashInput.append(auditLog.getServiceName());
            hashInput.append(auditLog.getUserId());
            hashInput.append(auditLog.getAction());
            hashInput.append(auditLog.getResource());
            hashInput.append(auditLog.getResourceId());
            hashInput.append(auditLog.getTimestamp().toString());
            hashInput.append(auditLog.getIpAddress());
            hashInput.append(auditLog.getStatus().name());

            // Add enhanced metadata hashing
            if (auditLog.getMetadata() != null && !auditLog.getMetadata().isEmpty()) {
                hashInput.append("METADATA_START");
                auditLog.getMetadata().entrySet().stream()
                        .sorted(java.util.Map.Entry.comparingByKey())
                        .forEach(entry -> {
                            hashInput.append(entry.getKey()).append("=").append(entry.getValue()).append(";");
                        });
                hashInput.append("METADATA_END");
            }

            // Add previous hash if exists
            if (auditLog.getPreviousHash() != null) {
                hashInput.append(auditLog.getPreviousHash());
            }

            // Add chain information
            hashInput.append(auditLog.getChainId());
            hashInput.append(auditLog.getChainIndex());

            // Calculate SHA-256 hash
            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
            byte[] hashBytes = digest.digest(hashInput.toString().getBytes(StandardCharsets.UTF_8));

            // Return Base64 encoded hash
            return Base64.getEncoder().encodeToString(hashBytes);

        } catch (NoSuchAlgorithmException e) {
            logger.error("Hash algorithm not available: {}", e.getMessage());
            throw new RuntimeException("Hash calculation failed", e);
        }
    }

    /**
     * Calculate forensic hash that includes match score and decision for
     * immutability
     * 
     * @param auditLog         Audit log entry
     * @param matchScore       Face comparison match score (0.0 to 1.0)
     * @param decision         Face comparison decision (MATCH/NO_MATCH/UNCERTAIN)
     * @param biometricMetrics Additional biometric metrics (FRR, FAR, etc.)
     * @return Forensic SHA-256 hash as Base64 string
     */
    public String calculateForensicHash(AuditLog auditLog, Double matchScore, String decision,
            java.util.Map<String, Object> biometricMetrics) {
        try {
            // Create forensic hash input with all standard fields
            StringBuilder hashInput = new StringBuilder();
            hashInput.append("FORENSIC_HASH_V1.0|");
            hashInput.append(auditLog.getEventId()).append("|");
            hashInput.append(auditLog.getEventType()).append("|");
            hashInput.append(auditLog.getServiceName()).append("|");
            hashInput.append(auditLog.getUserId()).append("|");
            hashInput.append(auditLog.getAction()).append("|");
            hashInput.append(auditLog.getResource()).append("|");
            hashInput.append(auditLog.getResourceId()).append("|");
            hashInput.append(auditLog.getTimestamp().toString()).append("|");
            hashInput.append(auditLog.getIpAddress()).append("|");
            hashInput.append(auditLog.getStatus().name()).append("|");

            // Add critical forensic data that must be immutable
            hashInput.append("FORENSIC_DATA_START|");
            if (matchScore != null) {
                hashInput.append("MATCH_SCORE:").append(String.format("%.6f", matchScore)).append("|");
            }
            if (decision != null) {
                hashInput.append("DECISION:").append(decision).append("|");
            }

            // Add biometric metrics in sorted order for consistency
            if (biometricMetrics != null && !biometricMetrics.isEmpty()) {
                hashInput.append("BIOMETRIC_METRICS_START|");
                biometricMetrics.entrySet().stream()
                        .sorted(java.util.Map.Entry.comparingByKey())
                        .forEach(entry -> {
                            hashInput.append(entry.getKey()).append("=").append(entry.getValue()).append("|");
                        });
                hashInput.append("BIOMETRIC_METRICS_END|");
            }
            hashInput.append("FORENSIC_DATA_END|");

            // Add all metadata
            if (auditLog.getMetadata() != null && !auditLog.getMetadata().isEmpty()) {
                hashInput.append("METADATA_START|");
                auditLog.getMetadata().entrySet().stream()
                        .sorted(java.util.Map.Entry.comparingByKey())
                        .forEach(entry -> {
                            hashInput.append(entry.getKey()).append("=").append(entry.getValue()).append("|");
                        });
                hashInput.append("METADATA_END|");
            }

            // Add previous hash if exists (chain integrity)
            if (auditLog.getPreviousHash() != null) {
                hashInput.append("PREV_HASH:").append(auditLog.getPreviousHash()).append("|");
            }

            // Add chain information
            hashInput.append("CHAIN_ID:").append(auditLog.getChainId()).append("|");
            hashInput.append("CHAIN_INDEX:").append(auditLog.getChainIndex()).append("|");

            // Add timestamp for temporal integrity
            hashInput.append("HASH_TIMESTAMP:").append(java.time.Instant.now().toString());

            // Calculate SHA-256 hash
            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
            byte[] hashBytes = digest.digest(hashInput.toString().getBytes(StandardCharsets.UTF_8));

            // Return Base64 encoded hash
            String hash = Base64.getEncoder().encodeToString(hashBytes);

            logger.debug("Calculated forensic hash for event {} with matchScore={}, decision={}",
                    auditLog.getEventId(), matchScore, decision);

            return hash;

        } catch (NoSuchAlgorithmException e) {
            logger.error("Hash algorithm not available for forensic hash: {}", e.getMessage());
            throw new RuntimeException("Forensic hash calculation failed", e);
        }
    }

    /**
     * Calculate batch forensic hash for multiple face comparison results
     * 
     * @param auditLog     Audit log entry for the batch operation
     * @param batchResults List of batch comparison results
     * @return Batch forensic SHA-256 hash as Base64 string
     */
    public String calculateBatchForensicHash(AuditLog auditLog,
            java.util.List<java.util.Map<String, Object>> batchResults) {
        try {
            // Create batch forensic hash input
            StringBuilder hashInput = new StringBuilder();
            hashInput.append("BATCH_FORENSIC_HASH_V1.0|");
            hashInput.append(auditLog.getEventId()).append("|");
            hashInput.append(auditLog.getEventType()).append("|");
            hashInput.append(auditLog.getServiceName()).append("|");
            hashInput.append(auditLog.getUserId()).append("|");
            hashInput.append(auditLog.getAction()).append("|");
            hashInput.append(auditLog.getResource()).append("|");
            hashInput.append(auditLog.getResourceId()).append("|");
            hashInput.append(auditLog.getTimestamp().toString()).append("|");
            hashInput.append(auditLog.getIpAddress()).append("|");
            hashInput.append(auditLog.getStatus().name()).append("|");

            // Add batch results in deterministic order
            if (batchResults != null && !batchResults.isEmpty()) {
                hashInput.append("BATCH_RESULTS_START|");
                hashInput.append("BATCH_SIZE:").append(batchResults.size()).append("|");

                // Sort batch results by comparison_id for consistent hashing
                batchResults.stream()
                        .sorted((r1, r2) -> String.valueOf(r1.get("comparison_id"))
                                .compareTo(String.valueOf(r2.get("comparison_id"))))
                        .forEach(result -> {
                            hashInput.append("RESULT_START|");
                            hashInput.append("ID:").append(result.get("comparison_id")).append("|");
                            hashInput.append("SCORE:").append(String.format("%.6f", (Double) result.get("match_score")))
                                    .append("|");
                            hashInput.append("DECISION:").append(result.get("decision")).append("|");

                            // Add biometric metrics if present
                            if (result.containsKey("frr")) {
                                hashInput.append("FRR:").append(String.format("%.6f", (Double) result.get("frr")))
                                        .append("|");
                            }
                            if (result.containsKey("far")) {
                                hashInput.append("FAR:").append(String.format("%.6f", (Double) result.get("far")))
                                        .append("|");
                            }
                            if (result.containsKey("confidence_interval")) {
                                hashInput.append("CI:").append(result.get("confidence_interval")).append("|");
                            }

                            hashInput.append("RESULT_END|");
                        });

                hashInput.append("BATCH_RESULTS_END|");
            }

            // Add metadata
            if (auditLog.getMetadata() != null && !auditLog.getMetadata().isEmpty()) {
                hashInput.append("METADATA_START|");
                auditLog.getMetadata().entrySet().stream()
                        .sorted(java.util.Map.Entry.comparingByKey())
                        .forEach(entry -> {
                            hashInput.append(entry.getKey()).append("=").append(entry.getValue()).append("|");
                        });
                hashInput.append("METADATA_END|");
            }

            // Add chain information
            if (auditLog.getPreviousHash() != null) {
                hashInput.append("PREV_HASH:").append(auditLog.getPreviousHash()).append("|");
            }
            hashInput.append("CHAIN_ID:").append(auditLog.getChainId()).append("|");
            hashInput.append("CHAIN_INDEX:").append(auditLog.getChainIndex()).append("|");
            hashInput.append("HASH_TIMESTAMP:").append(java.time.Instant.now().toString());

            // Calculate SHA-256 hash
            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
            byte[] hashBytes = digest.digest(hashInput.toString().getBytes(StandardCharsets.UTF_8));

            // Return Base64 encoded hash
            String hash = Base64.getEncoder().encodeToString(hashBytes);

            logger.debug("Calculated batch forensic hash for event {} with {} results",
                    auditLog.getEventId(), batchResults != null ? batchResults.size() : 0);

            return hash;

        } catch (NoSuchAlgorithmException e) {
            logger.error("Hash algorithm not available for batch forensic hash: {}", e.getMessage());
            throw new RuntimeException("Batch forensic hash calculation failed", e);
        }
    }
}
