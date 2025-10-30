package com.forensic.storage.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Encryption Service for AES-256 encryption/decryption
 * 
 * This service provides:
 * - AES-256-GCM encryption for file content
 * - Key management and rotation
 * - Secure random key generation
 * - Base64 encoding for storage
 */
@Service
public class EncryptionService {

    private static final Logger logger = LoggerFactory.getLogger(EncryptionService.class);

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12; // 96 bits
    private static final int GCM_TAG_LENGTH = 16; // 128 bits
    private static final int KEY_LENGTH = 256; // 256 bits

    @Value("${encryption.master-key:forensic_encryption_key_32_chars_2024}")
    private String masterKey;

    // In-memory key cache (in production, use proper key management system)
    private final Map<String, SecretKey> keyCache = new ConcurrentHashMap<>();

    /**
     * Encrypt file content using AES-256-GCM
     * 
     * @param content File content to encrypt
     * @param keyId   Key identifier for encryption
     * @return Encrypted content as Base64 string
     */
    public String encrypt(byte[] content, String keyId) {
        try {
            SecretKey key = getOrCreateKey(keyId);

            // Generate random IV
            byte[] iv = new byte[GCM_IV_LENGTH];
            new SecureRandom().nextBytes(iv);

            // Initialize cipher
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec);

            // Encrypt content
            byte[] encryptedContent = cipher.doFinal(content);

            // Combine IV and encrypted content
            byte[] encryptedWithIv = new byte[GCM_IV_LENGTH + encryptedContent.length];
            System.arraycopy(iv, 0, encryptedWithIv, 0, GCM_IV_LENGTH);
            System.arraycopy(encryptedContent, 0, encryptedWithIv, GCM_IV_LENGTH, encryptedContent.length);

            // Encode to Base64
            String encryptedBase64 = Base64.getEncoder().encodeToString(encryptedWithIv);

            logger.debug("File encrypted successfully with key: {}", keyId);
            return encryptedBase64;

        } catch (Exception e) {
            logger.error("Failed to encrypt file content: {}", e.getMessage());
            throw new RuntimeException("Encryption failed", e);
        }
    }

    /**
     * Decrypt file content using AES-256-GCM
     * 
     * @param encryptedContent Encrypted content as Base64 string
     * @param keyId            Key identifier for decryption
     * @return Decrypted file content
     */
    public byte[] decrypt(String encryptedContent, String keyId) {
        try {
            SecretKey key = getOrCreateKey(keyId);

            // Decode from Base64
            byte[] encryptedWithIv = Base64.getDecoder().decode(encryptedContent);

            // Extract IV and encrypted content
            byte[] iv = new byte[GCM_IV_LENGTH];
            byte[] encrypted = new byte[encryptedWithIv.length - GCM_IV_LENGTH];
            System.arraycopy(encryptedWithIv, 0, iv, 0, GCM_IV_LENGTH);
            System.arraycopy(encryptedWithIv, GCM_IV_LENGTH, encrypted, 0, encrypted.length);

            // Initialize cipher
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec);

            // Decrypt content
            byte[] decryptedContent = cipher.doFinal(encrypted);

            logger.debug("File decrypted successfully with key: {}", keyId);
            return decryptedContent;

        } catch (Exception e) {
            logger.error("Failed to decrypt file content: {}", e.getMessage());
            throw new RuntimeException("Decryption failed", e);
        }
    }

    /**
     * Generate a new encryption key
     * 
     * @return Generated key as Base64 string
     */
    public String generateKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
            keyGenerator.init(KEY_LENGTH);
            SecretKey key = keyGenerator.generateKey();

            String keyBase64 = Base64.getEncoder().encodeToString(key.getEncoded());
            logger.info("New encryption key generated");
            return keyBase64;

        } catch (Exception e) {
            logger.error("Failed to generate encryption key: {}", e.getMessage());
            throw new RuntimeException("Key generation failed", e);
        }
    }

    /**
     * Get or create encryption key for given key ID
     * 
     * @param keyId Key identifier
     * @return SecretKey for encryption/decryption
     */
    private SecretKey getOrCreateKey(String keyId) {
        return keyCache.computeIfAbsent(keyId, k -> {
            try {
                // In production, retrieve key from secure key management system
                // For demo purposes, derive key from master key and key ID
                String keyMaterial = masterKey + "_" + keyId;
                byte[] keyBytes = keyMaterial.getBytes(StandardCharsets.UTF_8);

                // Ensure key is exactly 32 bytes (256 bits)
                byte[] truncatedKey = new byte[32];
                System.arraycopy(keyBytes, 0, truncatedKey, 0, Math.min(keyBytes.length, 32));

                return new SecretKeySpec(truncatedKey, ALGORITHM);

            } catch (Exception e) {
                logger.error("Failed to create key for ID {}: {}", keyId, e.getMessage());
                throw new RuntimeException("Key creation failed", e);
            }
        });
    }

    /**
     * Generate unique key ID for file
     * 
     * @param fileId File identifier
     * @return Unique key ID
     */
    public String generateKeyId(String fileId) {
        return "key_" + fileId + "_" + System.currentTimeMillis();
    }

    /**
     * Validate encryption key format
     * 
     * @param keyBase64 Key as Base64 string
     * @return True if key format is valid
     */
    public boolean validateKey(String keyBase64) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(keyBase64);
            return keyBytes.length == 32; // 256 bits
        } catch (Exception e) {
            logger.warn("Invalid key format: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Clear key cache (for security purposes)
     */
    public void clearKeyCache() {
        keyCache.clear();
        logger.info("Encryption key cache cleared");
    }

    /**
     * Get key cache size
     * 
     * @return Number of keys in cache
     */
    public int getKeyCacheSize() {
        return keyCache.size();
    }
}
