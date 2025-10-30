package com.forensic.storage.service;

import com.forensic.storage.entity.File;
import com.forensic.storage.entity.FileStatus;
import com.forensic.storage.entity.VirusScanStatus;
import com.forensic.storage.repository.FileRepository;
import io.minio.*;
import io.minio.errors.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * File Storage Service using MinIO for object storage
 * 
 * This service provides:
 * - File upload and download
 * - Encryption/decryption integration
 * - Metadata extraction
 * - Hash calculation
 * - Virus scanning
 */
@Service
public class FileStorageService {

    private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);

    @Autowired
    private MinioClient minioClient;

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private EncryptionService encryptionService;

    @Autowired
    private MetadataExtractionService metadataExtractionService;

    @Autowired
    private HashCalculationService hashCalculationService;

    @Value("${minio.bucket-name:forensic-files}")
    private String bucketName;

    /**
     * Upload file to storage
     * 
     * @param multipartFile File to upload
     * @param uploadedBy    User ID who uploaded the file
     * @param caseId        Case ID (optional)
     * @return File entity with metadata
     */
    public File uploadFile(MultipartFile multipartFile, UUID uploadedBy, UUID caseId) {
        try {
            // Validate file
            validateFile(multipartFile);

            // Generate file ID and paths
            UUID fileId = UUID.randomUUID();
            String originalFilename = multipartFile.getOriginalFilename();
            String storedFilename = generateStoredFilename(fileId, originalFilename);
            String filePath = generateFilePath(storedFilename);

            // Calculate file hash
            byte[] fileContent = multipartFile.getBytes();
            String fileHash = hashCalculationService.calculateSHA256(fileContent);

            // Check for duplicate files
            Optional<File> existingFileOpt = fileRepository.findByFileHash(fileHash);
            if (existingFileOpt.isPresent() && existingFileOpt.get().isActive()) {
                logger.warn("Duplicate file detected: {}", fileHash);
                // In production, you might want to handle this differently
            }

            // Extract metadata
            String metadata = metadataExtractionService.extractMetadata(multipartFile);

            // Encrypt file content
            String keyId = encryptionService.generateKeyId(fileId.toString());
            String encryptedContent = encryptionService.encrypt(fileContent, keyId);

            // Upload to MinIO
            uploadToMinIO(filePath, encryptedContent);

            // Create file entity
            File file = new File();
            file.setId(fileId);
            file.setOriginalFilename(originalFilename);
            file.setStoredFilename(storedFilename);
            file.setFilePath(filePath);
            file.setFileSize(multipartFile.getSize());
            file.setMimeType(multipartFile.getContentType());
            file.setFileHash(fileHash);
            file.setEncryptionKeyId(keyId);
            file.setCaseId(caseId);
            file.setUploadedBy(uploadedBy);
            file.setUploadDate(LocalDateTime.now());
            file.setMetadata(metadata);
            file.setVirusScanStatus(VirusScanStatus.PENDING);

            // Save to database
            File savedFile = fileRepository.save(file);

            logger.info("File uploaded successfully: {} (ID: {})", originalFilename, fileId);
            return savedFile;

        } catch (Exception e) {
            logger.error("Failed to upload file: {}", e.getMessage());
            throw new RuntimeException("File upload failed", e);
        }
    }

    /**
     * Download file from storage
     * 
     * @param fileId File ID to download
     * @param userId User ID requesting download
     * @return File content as byte array
     */
    public byte[] downloadFile(UUID fileId, UUID userId) {
        try {
            // Get file from database
            File file = fileRepository.findById(fileId)
                    .orElseThrow(() -> new RuntimeException("File not found"));

            // Check if file is active
            if (!file.isActive()) {
                throw new RuntimeException("File is not available");
            }

            // Check virus scan status
            if (file.isInfected()) {
                throw new RuntimeException("File is infected and cannot be downloaded");
            }

            // Download from MinIO
            String encryptedContent = downloadFromMinIO(file.getFilePath());

            // Decrypt file content
            byte[] decryptedContent = encryptionService.decrypt(encryptedContent, file.getEncryptionKeyId());

            // Update access statistics
            file.incrementAccessCount();
            fileRepository.save(file);

            logger.info("File downloaded successfully: {} (ID: {})", file.getOriginalFilename(), fileId);
            return decryptedContent;

        } catch (Exception e) {
            logger.error("Failed to download file {}: {}", fileId, e.getMessage());
            throw new RuntimeException("File download failed", e);
        }
    }

    /**
     * Delete file from storage
     * 
     * @param fileId File ID to delete
     * @param userId User ID requesting deletion
     */
    public void deleteFile(UUID fileId, UUID userId) {
        try {
            // Get file from database
            File file = fileRepository.findById(fileId)
                    .orElseThrow(() -> new RuntimeException("File not found"));

            // Soft delete - mark as deleted
            file.setFileStatus(FileStatus.DELETED);
            file.setDeletedAt(LocalDateTime.now());
            fileRepository.save(file);

            // In production, you might want to schedule actual deletion
            // for compliance with retention policies

            logger.info("File deleted successfully: {} (ID: {})", file.getOriginalFilename(), fileId);

        } catch (Exception e) {
            logger.error("Failed to delete file {}: {}", fileId, e.getMessage());
            throw new RuntimeException("File deletion failed", e);
        }
    }

    /**
     * Get file metadata
     * 
     * @param fileId File ID
     * @return File entity with metadata
     */
    public File getFileMetadata(UUID fileId) {
        try {
            return fileRepository.findById(fileId)
                    .orElseThrow(() -> new RuntimeException("File not found"));
        } catch (Exception e) {
            logger.error("Failed to get file metadata {}: {}", fileId, e.getMessage());
            throw new RuntimeException("Failed to get file metadata", e);
        }
    }

    /**
     * Validate uploaded file
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        // Check file size (max 100MB)
        long maxSize = 100 * 1024 * 1024; // 100MB
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("File size exceeds maximum allowed size");
        }

        // Check file type
        String contentType = file.getContentType();
        if (contentType == null || !isAllowedContentType(contentType)) {
            throw new IllegalArgumentException("File type not allowed");
        }

        // Check filename
        String filename = file.getOriginalFilename();
        if (filename == null || filename.trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid filename");
        }
    }

    /**
     * Check if content type is allowed
     */
    private boolean isAllowedContentType(String contentType) {
        String[] allowedTypes = {
                "image/jpeg", "image/png", "image/gif", "image/bmp", "image/tiff",
                "video/mp4", "video/avi", "video/mov", "video/wmv",
                "application/pdf", "text/plain"
        };

        for (String allowedType : allowedTypes) {
            if (contentType.startsWith(allowedType)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Generate stored filename
     */
    private String generateStoredFilename(UUID fileId, String originalFilename) {
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        return fileId.toString() + extension;
    }

    /**
     * Generate file path
     */
    private String generateFilePath(String storedFilename) {
        LocalDateTime now = LocalDateTime.now();
        String year = String.valueOf(now.getYear());
        String month = String.format("%02d", now.getMonthValue());
        String day = String.format("%02d", now.getDayOfMonth());

        return String.format("%s/%s/%s/%s", year, month, day, storedFilename);
    }

    /**
     * Upload content to MinIO
     */
    private void uploadToMinIO(String filePath, String encryptedContent) {
        try {
            // Ensure bucket exists
            if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }

            // Upload file
            try (InputStream inputStream = new ByteArrayInputStream(encryptedContent.getBytes())) {
                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(bucketName)
                                .object(filePath)
                                .stream(inputStream, encryptedContent.length(), -1)
                                .contentType("application/octet-stream")
                                .build());
            }

            logger.debug("File uploaded to MinIO: {}", filePath);

        } catch (Exception e) {
            logger.error("Failed to upload to MinIO: {}", e.getMessage());
            throw new RuntimeException("MinIO upload failed", e);
        }
    }

    /**
     * Download content from MinIO
     */
    private String downloadFromMinIO(String filePath) {
        try {
            try (InputStream inputStream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(filePath)
                            .build())) {
                return new String(inputStream.readAllBytes());
            }

        } catch (Exception e) {
            logger.error("Failed to download from MinIO: {}", e.getMessage());
            throw new RuntimeException("MinIO download failed", e);
        }
    }
}
