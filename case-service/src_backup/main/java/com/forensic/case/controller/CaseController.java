package com.forensic.case.controller;

import com.forensic.case.entity.Case;
import com.forensic.case.service.CaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

/**
 * Case Controller
 * 
 * Handles case management endpoints
 */
@RestController
@RequestMapping("/api/cases")
@Tag(name = "Case Management", description = "Case management APIs")
public class CaseController {

    private static final Logger logger = LoggerFactory.getLogger(CaseController.class);

    @Autowired
    private CaseService caseService;

    /**
     * Create a new case
     */
    @PostMapping
    @Operation(summary = "Create case", description = "Create a new forensic case")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVESTIGATOR')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> createCase(@RequestBody Case caseEntity) {
        try {
            logger.info("Creating new case: {}", caseEntity.getTitle());
            
            Case createdCase = caseService.createCase(caseEntity);
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(createdCase);
                    
        } catch (Exception e) {
            logger.error("Failed to create case: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body("Failed to create case: " + e.getMessage());
        }
    }

    /**
     * Get case by ID
     */
    @GetMapping("/{caseId}")
    @Operation(summary = "Get case by ID", description = "Get a case by its ID")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVESTIGATOR', 'ANALYST', 'VIEWER')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> getCaseById(@PathVariable UUID caseId) {
        try {
            Optional<Case> caseEntity = caseService.getCaseById(caseId);
            
            if (caseEntity.isPresent()) {
                return ResponseEntity.ok(caseEntity.get());
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            logger.error("Failed to get case {}: {}", caseId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body("Failed to get case: " + e.getMessage());
        }
    }

    /**
     * Get case by case number
     */
    @GetMapping("/number/{caseNumber}")
    @Operation(summary = "Get case by number", description = "Get a case by its case number")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVESTIGATOR', 'ANALYST', 'VIEWER')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> getCaseByNumber(@PathVariable String caseNumber) {
        try {
            Optional<Case> caseEntity = caseService.getCaseByNumber(caseNumber);
            
            if (caseEntity.isPresent()) {
                return ResponseEntity.ok(caseEntity.get());
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            logger.error("Failed to get case by number {}: {}", caseNumber, e.getMessage());
            return ResponseEntity.badRequest()
                    .body("Failed to get case: " + e.getMessage());
        }
    }

    /**
     * Update case
     */
    @PutMapping("/{caseId}")
    @Operation(summary = "Update case", description = "Update an existing case")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVESTIGATOR')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> updateCase(@PathVariable UUID caseId, @RequestBody Case caseEntity) {
        try {
            caseEntity.setId(caseId);
            Case updatedCase = caseService.updateCase(caseEntity);
            
            return ResponseEntity.ok(updatedCase);
            
        } catch (Exception e) {
            logger.error("Failed to update case {}: {}", caseId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body("Failed to update case: " + e.getMessage());
        }
    }

    /**
     * Delete case
     */
    @DeleteMapping("/{caseId}")
    @Operation(summary = "Delete case", description = "Delete a case (soft delete)")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVESTIGATOR')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> deleteCase(@PathVariable UUID caseId) {
        try {
            caseService.deleteCase(caseId);
            
            return ResponseEntity.ok("Case deleted successfully");
            
        } catch (Exception e) {
            logger.error("Failed to delete case {}: {}", caseId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body("Failed to delete case: " + e.getMessage());
        }
    }

    /**
     * Get all cases with pagination
     */
    @GetMapping
    @Operation(summary = "Get all cases", description = "Get all cases with pagination")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVESTIGATOR', 'ANALYST', 'VIEWER')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> getAllCases(Pageable pageable) {
        try {
            Page<Case> cases = caseService.getAllCases(pageable);
            return ResponseEntity.ok(cases);
            
        } catch (Exception e) {
            logger.error("Failed to get all cases: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body("Failed to get cases: " + e.getMessage());
        }
    }

    /**
     * Get cases by investigator
     */
    @GetMapping("/investigator/{investigatorId}")
    @Operation(summary = "Get cases by investigator", description = "Get cases assigned to a specific investigator")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVESTIGATOR', 'ANALYST', 'VIEWER')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> getCasesByInvestigator(@PathVariable UUID investigatorId, Pageable pageable) {
        try {
            Page<Case> cases = caseService.getCasesByInvestigator(investigatorId, pageable);
            return ResponseEntity.ok(cases);
            
        } catch (Exception e) {
            logger.error("Failed to get cases by investigator {}: {}", investigatorId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body("Failed to get cases: " + e.getMessage());
        }
    }

    /**
     * Get cases by status
     */
    @GetMapping("/status/{status}")
    @Operation(summary = "Get cases by status", description = "Get cases with a specific status")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVESTIGATOR', 'ANALYST', 'VIEWER')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> getCasesByStatus(@PathVariable CaseStatus status, Pageable pageable) {
        try {
            Page<Case> cases = caseService.getCasesByStatus(status, pageable);
            return ResponseEntity.ok(cases);
            
        } catch (Exception e) {
            logger.error("Failed to get cases by status {}: {}", status, e.getMessage());
            return ResponseEntity.badRequest()
                    .body("Failed to get cases: " + e.getMessage());
        }
    }

    /**
     * Get cases by priority
     */
    @GetMapping("/priority/{priority}")
    @Operation(summary = "Get cases by priority", description = "Get cases with a specific priority")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVESTIGATOR', 'ANALYST', 'VIEWER')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> getCasesByPriority(@PathVariable CasePriority priority, Pageable pageable) {
        try {
            Page<Case> cases = caseService.getCasesByPriority(priority, pageable);
            return ResponseEntity.ok(cases);
            
        } catch (Exception e) {
            logger.error("Failed to get cases by priority {}: {}", priority, e.getMessage());
            return ResponseEntity.badRequest()
                    .body("Failed to get cases: " + e.getMessage());
        }
    }

    /**
     * Search cases
     */
    @GetMapping("/search")
    @Operation(summary = "Search cases", description = "Search cases by title or description")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVESTIGATOR', 'ANALYST', 'VIEWER')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> searchCases(@RequestParam String searchTerm, Pageable pageable) {
        try {
            Page<Case> cases = caseService.searchCases(searchTerm, pageable);
            return ResponseEntity.ok(cases);
            
        } catch (Exception e) {
            logger.error("Failed to search cases with term {}: {}", searchTerm, e.getMessage());
            return ResponseEntity.badRequest()
                    .body("Failed to search cases: " + e.getMessage());
        }
    }

    /**
     * Get case statistics
     */
    @GetMapping("/statistics")
    @Operation(summary = "Get case statistics", description = "Get overall case statistics")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVESTIGATOR', 'ANALYST', 'VIEWER')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> getCaseStatistics() {
        try {
            CaseService.CaseStatistics statistics = caseService.getCaseStatistics();
            return ResponseEntity.ok(statistics);
            
        } catch (Exception e) {
            logger.error("Failed to get case statistics: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body("Failed to get case statistics: " + e.getMessage());
        }
    }

    /**
     * Get face comparisons for a case
     */
    @GetMapping("/{caseId}/comparisons")
    @Operation(summary = "Get face comparisons for case", description = "Get face comparisons for a specific case")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVESTIGATOR', 'ANALYST', 'VIEWER')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> getFaceComparisonsByCase(@PathVariable UUID caseId, Pageable pageable) {
        try {
            Page<FaceComparison> comparisons = caseService.getFaceComparisonsByCase(caseId, pageable);
            return ResponseEntity.ok(comparisons);
            
        } catch (Exception e) {
            logger.error("Failed to get face comparisons for case {}: {}", caseId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body("Failed to get face comparisons: " + e.getMessage());
        }
    }
}
