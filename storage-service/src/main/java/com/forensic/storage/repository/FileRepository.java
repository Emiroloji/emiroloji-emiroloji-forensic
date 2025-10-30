package com.forensic.storage.repository;

import com.forensic.storage.entity.File;
import com.forensic.storage.entity.FileStatus;
import com.forensic.storage.entity.VirusScanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * File Repository Interface
 * 
 * Provides data access methods for File entities
 */
@Repository
public interface FileRepository extends JpaRepository<File, UUID> {

    /**
     * Find file by hash
     */
    Optional<File> findByFileHash(String fileHash);

    /**
     * Find files by case ID
     */
    List<File> findByCaseId(UUID caseId);

    /**
     * Find files by uploaded by user
     */
    List<File> findByUploadedBy(UUID uploadedBy);

    /**
     * Find files by MIME type
     */
    List<File> findByMimeType(String mimeType);

    /**
     * Find files by file status
     */
    List<File> findByFileStatus(FileStatus fileStatus);

    /**
     * Find files by virus scan status
     */
    List<File> findByVirusScanStatus(VirusScanStatus virusScanStatus);

    /**
     * Find files uploaded after a specific date
     */
    List<File> findByUploadDateAfter(LocalDateTime date);

    /**
     * Find files uploaded before a specific date
     */
    List<File> findByUploadDateBefore(LocalDateTime date);

    /**
     * Find files by size range
     */
    @Query("SELECT f FROM File f WHERE f.fileSize BETWEEN :minSize AND :maxSize")
    List<File> findByFileSizeBetween(@Param("minSize") Long minSize, @Param("maxSize") Long maxSize);

    /**
     * Find files by original filename pattern
     */
    @Query("SELECT f FROM File f WHERE f.originalFilename LIKE %:pattern%")
    List<File> findByOriginalFilenameContaining(@Param("pattern") String pattern);

    /**
     * Find active files
     */
    @Query("SELECT f FROM File f WHERE f.fileStatus = 'ACTIVE' AND f.deletedAt IS NULL")
    List<File> findActiveFiles();

    /**
     * Find deleted files
     */
    @Query("SELECT f FROM File f WHERE f.fileStatus = 'DELETED' OR f.deletedAt IS NOT NULL")
    List<File> findDeletedFiles();

    /**
     * Find files that need virus scanning
     */
    @Query("SELECT f FROM File f WHERE f.virusScanStatus = 'PENDING'")
    List<File> findFilesNeedingVirusScan();

    /**
     * Find infected files
     */
    @Query("SELECT f FROM File f WHERE f.virusScanStatus = 'INFECTED'")
    List<File> findInfectedFiles();

    /**
     * Count files by case ID
     */
    long countByCaseId(UUID caseId);

    /**
     * Count files by uploaded by user
     */
    long countByUploadedBy(UUID uploadedBy);

    /**
     * Count files by MIME type
     */
    long countByMimeType(String mimeType);

    /**
     * Count active files
     */
    @Query("SELECT COUNT(f) FROM File f WHERE f.fileStatus = 'ACTIVE' AND f.deletedAt IS NULL")
    long countActiveFiles();

    /**
     * Count files uploaded after a specific date
     */
    long countByUploadDateAfter(LocalDateTime date);

    /**
     * Find files with high access count
     */
    @Query("SELECT f FROM File f WHERE f.accessCount > :threshold ORDER BY f.accessCount DESC")
    List<File> findFilesWithHighAccessCount(@Param("threshold") Integer threshold);

    /**
     * Find files not accessed recently
     */
    @Query("SELECT f FROM File f WHERE f.lastAccessed IS NULL OR f.lastAccessed < :date")
    List<File> findFilesNotAccessedSince(@Param("date") LocalDateTime date);

    /**
     * Find files by encryption key ID
     */
    List<File> findByEncryptionKeyId(String encryptionKeyId);

    /**
     * Find files by stored filename
     */
    Optional<File> findByStoredFilename(String storedFilename);

    /**
     * Find files by file path
     */
    Optional<File> findByFilePath(String filePath);

    /**
     * Find files uploaded in date range
     */
    @Query("SELECT f FROM File f WHERE f.uploadDate BETWEEN :startDate AND :endDate")
    List<File> findByUploadDateBetween(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Find files by case ID and file status
     */
    @Query("SELECT f FROM File f WHERE f.caseId = :caseId AND f.fileStatus = :fileStatus")
    List<File> findByCaseIdAndFileStatus(@Param("caseId") UUID caseId, @Param("fileStatus") FileStatus fileStatus);

    /**
     * Find files by uploaded by user and file status
     */
    @Query("SELECT f FROM File f WHERE f.uploadedBy = :uploadedBy AND f.fileStatus = :fileStatus")
    List<File> findByUploadedByAndFileStatus(@Param("uploadedBy") UUID uploadedBy,
            @Param("fileStatus") FileStatus fileStatus);

    /**
     * Find files by MIME type and file status
     */
    @Query("SELECT f FROM File f WHERE f.mimeType = :mimeType AND f.fileStatus = :fileStatus")
    List<File> findByMimeTypeAndFileStatus(@Param("mimeType") String mimeType,
            @Param("fileStatus") FileStatus fileStatus);

    /**
     * Find files with specific metadata
     */
    @Query("SELECT f FROM File f WHERE f.metadata LIKE %:metadataPattern%")
    List<File> findByMetadataContaining(@Param("metadataPattern") String metadataPattern);

    /**
     * Find files by size category
     */
    @Query("SELECT f FROM File f WHERE " +
            "CASE " +
            "WHEN f.fileSize < 1024 THEN 'tiny' " +
            "WHEN f.fileSize < 1048576 THEN 'small' " +
            "WHEN f.fileSize < 10485760 THEN 'medium' " +
            "WHEN f.fileSize < 104857600 THEN 'large' " +
            "ELSE 'very_large' " +
            "END = :category")
    List<File> findBySizeCategory(@Param("category") String category);

    /**
     * Find duplicate files (same hash)
     */
    @Query("SELECT f FROM File f WHERE f.fileHash IN " +
            "(SELECT f2.fileHash FROM File f2 WHERE f2.fileStatus = 'ACTIVE' " +
            "GROUP BY f2.fileHash HAVING COUNT(f2) > 1)")
    List<File> findDuplicateFiles();

    /**
     * Find files for cleanup (old deleted files)
     */
    @Query("SELECT f FROM File f WHERE f.fileStatus = 'DELETED' AND f.deletedAt < :cutoffDate")
    List<File> findFilesForCleanup(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Find files by access count range
     */
    @Query("SELECT f FROM File f WHERE f.accessCount BETWEEN :minAccess AND :maxAccess")
    List<File> findByAccessCountBetween(@Param("minAccess") Integer minAccess, @Param("maxAccess") Integer maxAccess);

    /**
     * Find files with no access count
     */
    @Query("SELECT f FROM File f WHERE f.accessCount = 0 OR f.accessCount IS NULL")
    List<File> findFilesWithNoAccess();

    /**
     * Find files by virus scan date
     */
    @Query("SELECT f FROM File f WHERE f.virusScanDate BETWEEN :startDate AND :endDate")
    List<File> findByVirusScanDateBetween(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Find files that need virus scan update
     */
    @Query("SELECT f FROM File f WHERE f.virusScanStatus = 'PENDING' AND f.uploadDate < :cutoffDate")
    List<File> findFilesNeedingVirusScanUpdate(@Param("cutoffDate") LocalDateTime cutoffDate);
}
