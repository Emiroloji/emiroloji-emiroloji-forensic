package com.forensic.storage.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * File entity for storing file metadata and information
 */
@Entity
@Table(name = "files", schema = "storage")
@EntityListeners(AuditingEntityListener.class)
public class File {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank
    @Size(max = 255)
    @Column(name = "original_filename", nullable = false)
    private String originalFilename;

    @NotBlank
    @Size(max = 255)
    @Column(name = "stored_filename", nullable = false)
    private String storedFilename;

    @NotBlank
    @Size(max = 500)
    @Column(name = "file_path", nullable = false)
    private String filePath;

    @NotNull
    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @NotBlank
    @Size(max = 100)
    @Column(name = "mime_type", nullable = false)
    private String mimeType;

    @NotBlank
    @Size(max = 64)
    @Column(name = "file_hash", nullable = false)
    private String fileHash; // SHA-256 hash

    @Size(max = 100)
    @Column(name = "encryption_key_id")
    private String encryptionKeyId;

    @Column(name = "case_id")
    private UUID caseId;

    @NotNull
    @Column(name = "uploaded_by", nullable = false)
    private UUID uploadedBy;

    @CreatedDate
    @Column(name = "upload_date", nullable = false, updatable = false)
    private LocalDateTime uploadDate;

    @Column(name = "last_accessed")
    private LocalDateTime lastAccessed;

    @Column(name = "access_count")
    private Integer accessCount = 0;

    @Column(name = "metadata", columnDefinition = "jsonb")
    private String metadata; // JSON metadata

    @Enumerated(EnumType.STRING)
    @Column(name = "virus_scan_status")
    private VirusScanStatus virusScanStatus = VirusScanStatus.PENDING;

    @Column(name = "virus_scan_date")
    private LocalDateTime virusScanDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "file_status")
    private FileStatus fileStatus = FileStatus.ACTIVE;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructors
    public File() {
    }

    public File(String originalFilename, String storedFilename, String filePath,
            Long fileSize, String mimeType, String fileHash, UUID uploadedBy) {
        this.originalFilename = originalFilename;
        this.storedFilename = storedFilename;
        this.filePath = filePath;
        this.fileSize = fileSize;
        this.mimeType = mimeType;
        this.fileHash = fileHash;
        this.uploadedBy = uploadedBy;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    public String getStoredFilename() {
        return storedFilename;
    }

    public void setStoredFilename(String storedFilename) {
        this.storedFilename = storedFilename;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getFileHash() {
        return fileHash;
    }

    public void setFileHash(String fileHash) {
        this.fileHash = fileHash;
    }

    public String getEncryptionKeyId() {
        return encryptionKeyId;
    }

    public void setEncryptionKeyId(String encryptionKeyId) {
        this.encryptionKeyId = encryptionKeyId;
    }

    public UUID getCaseId() {
        return caseId;
    }

    public void setCaseId(UUID caseId) {
        this.caseId = caseId;
    }

    public UUID getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedBy(UUID uploadedBy) {
        this.uploadedBy = uploadedBy;
    }

    public LocalDateTime getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(LocalDateTime uploadDate) {
        this.uploadDate = uploadDate;
    }

    public LocalDateTime getLastAccessed() {
        return lastAccessed;
    }

    public void setLastAccessed(LocalDateTime lastAccessed) {
        this.lastAccessed = lastAccessed;
    }

    public Integer getAccessCount() {
        return accessCount;
    }

    public void setAccessCount(Integer accessCount) {
        this.accessCount = accessCount;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public VirusScanStatus getVirusScanStatus() {
        return virusScanStatus;
    }

    public void setVirusScanStatus(VirusScanStatus virusScanStatus) {
        this.virusScanStatus = virusScanStatus;
    }

    public LocalDateTime getVirusScanDate() {
        return virusScanDate;
    }

    public void setVirusScanDate(LocalDateTime virusScanDate) {
        this.virusScanDate = virusScanDate;
    }

    public FileStatus getFileStatus() {
        return fileStatus;
    }

    public void setFileStatus(FileStatus fileStatus) {
        this.fileStatus = fileStatus;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Business methods
    public void incrementAccessCount() {
        this.accessCount++;
        this.lastAccessed = LocalDateTime.now();
    }

    public boolean isActive() {
        return fileStatus == FileStatus.ACTIVE && deletedAt == null;
    }

    public boolean isDeleted() {
        return fileStatus == FileStatus.DELETED || deletedAt != null;
    }

    public boolean isVirusScanned() {
        return virusScanStatus != VirusScanStatus.PENDING;
    }

    public boolean isClean() {
        return virusScanStatus == VirusScanStatus.CLEAN;
    }

    public boolean isInfected() {
        return virusScanStatus == VirusScanStatus.INFECTED;
    }

    public String getFileExtension() {
        if (originalFilename == null) {
            return "";
        }
        int lastDotIndex = originalFilename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return originalFilename.substring(lastDotIndex + 1).toLowerCase();
    }

    public String getFormattedFileSize() {
        if (fileSize == null) {
            return "0 B";
        }

        long bytes = fileSize;
        String[] units = { "B", "KB", "MB", "GB", "TB" };
        int unitIndex = 0;

        while (bytes >= 1024 && unitIndex < units.length - 1) {
            bytes /= 1024;
            unitIndex++;
        }

        return String.format("%.2f %s", (double) bytes, units[unitIndex]);
    }
}
