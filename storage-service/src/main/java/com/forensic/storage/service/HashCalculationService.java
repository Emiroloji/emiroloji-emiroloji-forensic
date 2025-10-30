package com.forensic.storage.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Hash Calculation Service
 * 
 * This service provides hash calculation functionality for file integrity
 * verification
 */
@Service
public class HashCalculationService {

    private static final Logger logger = LoggerFactory.getLogger(HashCalculationService.class);

    /**
     * Calculate SHA-256 hash of file content
     * 
     * @param content File content as byte array
     * @return SHA-256 hash as hex string
     */
    public String calculateSHA256(byte[] content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(content);
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            logger.error("SHA-256 algorithm not available: {}", e.getMessage());
            throw new RuntimeException("Hash calculation failed", e);
        }
    }

    /**
     * Calculate MD5 hash of file content
     * 
     * @param content File content as byte array
     * @return MD5 hash as hex string
     */
    public String calculateMD5(byte[] content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] hash = digest.digest(content);
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            logger.error("MD5 algorithm not available: {}", e.getMessage());
            throw new RuntimeException("Hash calculation failed", e);
        }
    }

    /**
     * Calculate SHA-1 hash of file content
     * 
     * @param content File content as byte array
     * @return SHA-1 hash as hex string
     */
    public String calculateSHA1(byte[] content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] hash = digest.digest(content);
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            logger.error("SHA-1 algorithm not available: {}", e.getMessage());
            throw new RuntimeException("Hash calculation failed", e);
        }
    }

    /**
     * Verify file integrity using hash
     * 
     * @param content      File content as byte array
     * @param expectedHash Expected hash value
     * @param algorithm    Hash algorithm (SHA-256, MD5, SHA-1)
     * @return True if hash matches, False otherwise
     */
    public boolean verifyIntegrity(byte[] content, String expectedHash, String algorithm) {
        try {
            String calculatedHash = calculateHash(content, algorithm);
            return calculatedHash.equalsIgnoreCase(expectedHash);
        } catch (Exception e) {
            logger.error("Failed to verify integrity: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Calculate hash using specified algorithm
     * 
     * @param content   File content as byte array
     * @param algorithm Hash algorithm
     * @return Hash as hex string
     */
    private String calculateHash(byte[] content, String algorithm) {
        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            byte[] hash = digest.digest(content);
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            logger.error("Hash algorithm {} not available: {}", algorithm, e.getMessage());
            throw new RuntimeException("Hash calculation failed", e);
        }
    }
}
