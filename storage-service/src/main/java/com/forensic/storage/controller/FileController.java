package com.forensic.storage.controller;

import com.forensic.storage.entity.File;
import com.forensic.storage.service.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * File Controller
 * 
 * Handles file upload, download, and management endpoints
 */
@RestController
@RequestMapping("/api/storage")
@Tag(name = "File Storage", description = "File storage and management APIs")
public class FileController {

    private static final Logger logger = LoggerFactory.getLogger(FileController.class);

    @Autowired
    private FileStorageService fileStorageService;

    /**
     * Upload file
     */
    @PostMapping("/upload")
    @Operation(summary = "Upload file", description = "Upload a file to secure storage")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVESTIGATOR', 'ANALYST')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "caseId", required = false) UUID caseId,
            @RequestParam("uploadedBy") UUID uploadedBy) {

        try {
            logger.info("File upload request: {} by user {}", file.getOriginalFilename(), uploadedBy);

            File uploadedFile = fileStorageService.uploadFile(file, uploadedBy, caseId);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(uploadedFile);

        } catch (Exception e) {
            logger.error("File upload failed: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body("File upload failed: " + e.getMessage());
        }
    }

    /**
     * Download file
     */
    @GetMapping("/download/{fileId}")
    @Operation(summary = "Download file", description = "Download a file from secure storage")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVESTIGATOR', 'ANALYST', 'VIEWER')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> downloadFile(
            @PathVariable UUID fileId,
            @RequestParam("userId") UUID userId) {

        try {
            logger.info("File download request: {} by user {}", fileId, userId);

            File fileMetadata = fileStorageService.getFileMetadata(fileId);
            byte[] fileContent = fileStorageService.downloadFile(fileId, userId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(fileMetadata.getMimeType()));
            headers.setContentDispositionFormData("attachment", fileMetadata.getOriginalFilename());
            headers.setContentLength(fileContent.length);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(fileContent);

        } catch (Exception e) {
            logger.error("File download failed: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body("File download failed: " + e.getMessage());
        }
    }

    /**
     * Get file metadata
     */
    @GetMapping("/metadata/{fileId}")
    @Operation(summary = "Get file metadata", description = "Get metadata for a file")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVESTIGATOR', 'ANALYST', 'VIEWER')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> getFileMetadata(@PathVariable UUID fileId) {
        try {
            File fileMetadata = fileStorageService.getFileMetadata(fileId);
            return ResponseEntity.ok(fileMetadata);
        } catch (Exception e) {
            logger.error("Failed to get file metadata: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body("Failed to get file metadata: " + e.getMessage());
        }
    }

    /**
     * Delete file
     */
    @DeleteMapping("/{fileId}")
    @Operation(summary = "Delete file", description = "Delete a file from storage")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVESTIGATOR')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> deleteFile(
            @PathVariable UUID fileId,
            @RequestParam("userId") UUID userId) {

        try {
            logger.info("File deletion request: {} by user {}", fileId, userId);

            fileStorageService.deleteFile(fileId, userId);

            return ResponseEntity.ok("File deleted successfully");

        } catch (Exception e) {
            logger.error("File deletion failed: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body("File deletion failed: " + e.getMessage());
        }
    }
}
