package com.forensic.case.controller;

import com.forensic.case.entity.FaceComparison;
import com.forensic.case.service.FaceComparisonService;
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
 * Face Comparison Controller
 * 
 * Handles face comparison analysis endpoints
 */
@RestController
@RequestMapping("/api/face-comparisons")
@Tag(name = "Face Comparison", description = "Face comparison analysis APIs")
public class FaceComparisonController {

    private static final Logger logger = LoggerFactory.getLogger(FaceComparisonController.class);

    @Autowired
    private FaceComparisonService faceComparisonService;

    /**
     * Perform face comparison analysis
     */
    @PostMapping("/analyze")
    @Operation(summary = "Perform face comparison", description = "Perform face comparison analysis between two images")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVESTIGATOR', 'ANALYST')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> performFaceComparison(
            @RequestParam UUID caseId,
            @RequestParam UUID image1Id,
            @RequestParam UUID image2Id,
            @RequestParam(required = false) String comparisonName,
            @RequestParam(required = false) Double threshold,
            @RequestParam UUID analyzedBy,
            @RequestHeader("Authorization") String authToken) {
        
        try {
            logger.info("Starting face comparison analysis for case {}: {} vs {}", caseId, image1Id, image2Id);
            
            FaceComparison result = faceComparisonService.performFaceComparison(
                caseId, image1Id, image2Id, comparisonName, threshold, analyzedBy, authToken
            );
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(result);
                    
        } catch (Exception e) {
            logger.error("Face comparison analysis failed: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body("Face comparison analysis failed: " + e.getMessage());
        }
    }

    /**
     * Get face comparison by ID
     */
    @GetMapping("/{comparisonId}")
    @Operation(summary = "Get face comparison by ID", description = "Get a face comparison by its ID")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVESTIGATOR', 'ANALYST', 'VIEWER')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> getFaceComparisonById(@PathVariable UUID comparisonId) {
        try {
            Optional<FaceComparison> comparison = faceComparisonService.getFaceComparisonById(comparisonId);
            
            if (comparison.isPresent()) {
                return ResponseEntity.ok(comparison.get());
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            logger.error("Failed to get face comparison {}: {}", comparisonId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body("Failed to get face comparison: " + e.getMessage());
        }
    }

    /**
     * Get face comparisons by case ID
     */
    @GetMapping("/case/{caseId}")
    @Operation(summary = "Get face comparisons by case", description = "Get face comparisons for a specific case")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVESTIGATOR', 'ANALYST', 'VIEWER')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> getFaceComparisonsByCase(@PathVariable UUID caseId, Pageable pageable) {
        try {
            Page<FaceComparison> comparisons = faceComparisonService.getFaceComparisonsByCase(caseId, pageable);
            return ResponseEntity.ok(comparisons);
            
        } catch (Exception e) {
            logger.error("Failed to get face comparisons for case {}: {}", caseId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body("Failed to get face comparisons: " + e.getMessage());
        }
    }

    /**
     * Get face comparisons by analyzer
     */
    @GetMapping("/analyzer/{analyzedBy}")
    @Operation(summary = "Get face comparisons by analyzer", description = "Get face comparisons performed by a specific user")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVESTIGATOR', 'ANALYST', 'VIEWER')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> getFaceComparisonsByAnalyzer(@PathVariable UUID analyzedBy, Pageable pageable) {
        try {
            Page<FaceComparison> comparisons = faceComparisonService.getFaceComparisonsByAnalyzer(analyzedBy, pageable);
            return ResponseEntity.ok(comparisons);
            
        } catch (Exception e) {
            logger.error("Failed to get face comparisons by analyzer {}: {}", analyzedBy, e.getMessage());
            return ResponseEntity.badRequest()
                    .body("Failed to get face comparisons: " + e.getMessage());
        }
    }

    /**
     * Get face comparisons by decision
     */
    @GetMapping("/decision/{decision}")
    @Operation(summary = "Get face comparisons by decision", description = "Get face comparisons with a specific decision")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVESTIGATOR', 'ANALYST', 'VIEWER')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> getFaceComparisonsByDecision(@PathVariable ComparisonDecision decision, Pageable pageable) {
        try {
            Page<FaceComparison> comparisons = faceComparisonService.getFaceComparisonsByDecision(decision, pageable);
            return ResponseEntity.ok(comparisons);
            
        } catch (Exception e) {
            logger.error("Failed to get face comparisons by decision {}: {}", decision, e.getMessage());
            return ResponseEntity.badRequest()
                    .body("Failed to get face comparisons: " + e.getMessage());
        }
    }

    /**
     * Get face comparisons by confidence level
     */
    @GetMapping("/confidence/{confidenceLevel}")
    @Operation(summary = "Get face comparisons by confidence level", description = "Get face comparisons with a specific confidence level")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVESTIGATOR', 'ANALYST', 'VIEWER')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> getFaceComparisonsByConfidenceLevel(@PathVariable ConfidenceLevel confidenceLevel, Pageable pageable) {
        try {
            Page<FaceComparison> comparisons = faceComparisonService.getFaceComparisonsByConfidenceLevel(confidenceLevel, pageable);
            return ResponseEntity.ok(comparisons);
            
        } catch (Exception e) {
            logger.error("Failed to get face comparisons by confidence level {}: {}", confidenceLevel, e.getMessage());
            return ResponseEntity.badRequest()
                    .body("Failed to get face comparisons: " + e.getMessage());
        }
    }

    /**
     * Search face comparisons
     */
    @GetMapping("/search")
    @Operation(summary = "Search face comparisons", description = "Search face comparisons by name")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVESTIGATOR', 'ANALYST', 'VIEWER')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> searchFaceComparisons(@RequestParam String comparisonName, Pageable pageable) {
        try {
            Page<FaceComparison> comparisons = faceComparisonService.searchFaceComparisons(comparisonName, pageable);
            return ResponseEntity.ok(comparisons);
            
        } catch (Exception e) {
            logger.error("Failed to search face comparisons with name {}: {}", comparisonName, e.getMessage());
            return ResponseEntity.badRequest()
                    .body("Failed to search face comparisons: " + e.getMessage());
        }
    }

    /**
     * Get face comparison statistics
     */
    @GetMapping("/statistics")
    @Operation(summary = "Get face comparison statistics", description = "Get overall face comparison statistics")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVESTIGATOR', 'ANALYST', 'VIEWER')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> getFaceComparisonStatistics() {
        try {
            FaceComparisonService.FaceComparisonStatistics statistics = faceComparisonService.getFaceComparisonStatistics();
            return ResponseEntity.ok(statistics);
            
        } catch (Exception e) {
            logger.error("Failed to get face comparison statistics: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body("Failed to get face comparison statistics: " + e.getMessage());
        }
    }

    /**
     * Get face comparison statistics by case
     */
    @GetMapping("/statistics/case/{caseId}")
    @Operation(summary = "Get face comparison statistics by case", description = "Get face comparison statistics for a specific case")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVESTIGATOR', 'ANALYST', 'VIEWER')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> getFaceComparisonStatisticsByCase(@PathVariable UUID caseId) {
        try {
            FaceComparisonService.FaceComparisonStatistics statistics = faceComparisonService.getFaceComparisonStatisticsByCase(caseId);
            return ResponseEntity.ok(statistics);
            
        } catch (Exception e) {
            logger.error("Failed to get face comparison statistics for case {}: {}", caseId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body("Failed to get face comparison statistics: " + e.getMessage());
        }
    }
}
