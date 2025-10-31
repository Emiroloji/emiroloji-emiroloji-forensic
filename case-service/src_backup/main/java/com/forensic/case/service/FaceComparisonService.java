package com.forensic.case.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.forensic.case.entity.FaceComparison;
import com.forensic.case.repository.FaceComparisonRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Face Comparison Service for managing face comparison analyses
 * 
 * This service provides:
 * - Face comparison analysis
 * - Integration with AI service
 * - Comparison result management
 * - Batch processing capabilities
 * - Report generation
 */
@Service
@Transactional
public class FaceComparisonService {

    private static final Logger logger = LoggerFactory.getLogger(FaceComparisonService.class);

    @Autowired
    private FaceComparisonRepository faceComparisonRepository;

    @Autowired
    private AIServiceClient aiServiceClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Perform face comparison analysis
     * 
     * @param caseId Case ID
     * @param image1Id First image ID
     * @param image2Id Second image ID
     * @param comparisonName Comparison name
     * @param threshold Comparison threshold
     * @param analyzedBy User ID who performed the analysis
     * @param authToken Authentication token
     * @return Face comparison result
     */
    public FaceComparison performFaceComparison(UUID caseId, UUID image1Id, UUID image2Id, 
                                               String comparisonName, Double threshold, 
                                               UUID analyzedBy, String authToken) {
        try {
            logger.info("Starting face comparison analysis for case {}: {} vs {}", caseId, image1Id, image2Id);

            // Create face comparison entity
            FaceComparison faceComparison = new FaceComparison(caseId, image1Id, image2Id, analyzedBy);
            faceComparison.setComparisonName(comparisonName);

            // Call AI service for analysis
            JsonNode analysisResult = aiServiceClient.compareFaces(image1Id, image2Id, threshold, authToken)
                .block(); // Blocking call for simplicity

            if (analysisResult == null) {
                throw new RuntimeException("AI service returned null result");
            }

            // Extract results from AI service response
            extractAnalysisResults(faceComparison, analysisResult);

            // Save to database
            FaceComparison savedComparison = faceComparisonRepository.save(faceComparison);

            logger.info("Face comparison analysis completed: {} (ID: {})", 
                savedComparison.getComparisonName(), savedComparison.getId());

            return savedComparison;

        } catch (Exception e) {
            logger.error("Face comparison analysis failed: {}", e.getMessage());
            throw new RuntimeException("Face comparison analysis failed", e);
        }
    }

    /**
     * Get face comparison by ID
     * 
     * @param comparisonId Comparison ID
     * @return Face comparison entity
     */
    @Transactional(readOnly = true)
    public Optional<FaceComparison> getFaceComparisonById(UUID comparisonId) {
        try {
            return faceComparisonRepository.findById(comparisonId);
        } catch (Exception e) {
            logger.error("Failed to get face comparison by ID {}: {}", comparisonId, e.getMessage());
            throw new RuntimeException("Failed to get face comparison", e);
        }
    }

    /**
     * Get face comparisons by case ID
     * 
     * @param caseId Case ID
     * @param pageable Pagination parameters
     * @return Page of face comparisons
     */
    @Transactional(readOnly = true)
    public Page<FaceComparison> getFaceComparisonsByCase(UUID caseId, Pageable pageable) {
        try {
            return faceComparisonRepository.findByCaseId(caseId, pageable);
        } catch (Exception e) {
            logger.error("Failed to get face comparisons for case {}: {}", caseId, e.getMessage());
            throw new RuntimeException("Failed to get face comparisons", e);
        }
    }

    /**
     * Get face comparisons by analyzer
     * 
     * @param analyzedBy Analyzer user ID
     * @param pageable Pagination parameters
     * @return Page of face comparisons
     */
    @Transactional(readOnly = true)
    public Page<FaceComparison> getFaceComparisonsByAnalyzer(UUID analyzedBy, Pageable pageable) {
        try {
            return faceComparisonRepository.findByAnalyzedBy(analyzedBy, pageable);
        } catch (Exception e) {
            logger.error("Failed to get face comparisons by analyzer {}: {}", analyzedBy, e.getMessage());
            throw new RuntimeException("Failed to get face comparisons", e);
        }
    }

    /**
     * Get face comparisons by decision
     * 
     * @param decision Comparison decision
     * @param pageable Pagination parameters
     * @return Page of face comparisons
     */
    @Transactional(readOnly = true)
    public Page<FaceComparison> getFaceComparisonsByDecision(ComparisonDecision decision, Pageable pageable) {
        try {
            return faceComparisonRepository.findByDecision(decision, pageable);
        } catch (Exception e) {
            logger.error("Failed to get face comparisons by decision {}: {}", decision, e.getMessage());
            throw new RuntimeException("Failed to get face comparisons", e);
        }
    }

    /**
     * Get face comparisons by confidence level
     * 
     * @param confidenceLevel Confidence level
     * @param pageable Pagination parameters
     * @return Page of face comparisons
     */
    @Transactional(readOnly = true)
    public Page<FaceComparison> getFaceComparisonsByConfidenceLevel(ConfidenceLevel confidenceLevel, Pageable pageable) {
        try {
            return faceComparisonRepository.findByConfidenceLevel(confidenceLevel, pageable);
        } catch (Exception e) {
            logger.error("Failed to get face comparisons by confidence level {}: {}", confidenceLevel, e.getMessage());
            throw new RuntimeException("Failed to get face comparisons", e);
        }
    }

    /**
     * Search face comparisons by name
     * 
     * @param comparisonName Comparison name
     * @param pageable Pagination parameters
     * @return Page of face comparisons
     */
    @Transactional(readOnly = true)
    public Page<FaceComparison> searchFaceComparisons(String comparisonName, Pageable pageable) {
        try {
            return faceComparisonRepository.findByComparisonNameContainingIgnoreCase(comparisonName, pageable);
        } catch (Exception e) {
            logger.error("Failed to search face comparisons with name {}: {}", comparisonName, e.getMessage());
            throw new RuntimeException("Face comparison search failed", e);
        }
    }

    /**
     * Get face comparison statistics
     * 
     * @return Face comparison statistics
     */
    @Transactional(readOnly = true)
    public FaceComparisonStatistics getFaceComparisonStatistics() {
        try {
            long totalComparisons = faceComparisonRepository.count();
            long matchComparisons = faceComparisonRepository.countByDecision(ComparisonDecision.MATCH);
            long noMatchComparisons = faceComparisonRepository.countByDecision(ComparisonDecision.NO_MATCH);
            long uncertainComparisons = faceComparisonRepository.countByDecision(ComparisonDecision.UNCERTAIN);

            long highConfidenceComparisons = faceComparisonRepository.countByConfidenceLevel(ConfidenceLevel.HIGH);
            long veryHighConfidenceComparisons = faceComparisonRepository.countByConfidenceLevel(ConfidenceLevel.VERY_HIGH);

            return new FaceComparisonStatistics(
                totalComparisons, matchComparisons, noMatchComparisons, uncertainComparisons,
                highConfidenceComparisons, veryHighConfidenceComparisons
            );

        } catch (Exception e) {
            logger.error("Failed to get face comparison statistics: {}", e.getMessage());
            throw new RuntimeException("Failed to get face comparison statistics", e);
        }
    }

    /**
     * Get face comparison statistics by case
     * 
     * @param caseId Case ID
     * @return Face comparison statistics for the case
     */
    @Transactional(readOnly = true)
    public FaceComparisonStatistics getFaceComparisonStatisticsByCase(UUID caseId) {
        try {
            long totalComparisons = faceComparisonRepository.countByCaseId(caseId);
            long matchComparisons = faceComparisonRepository.countByCaseIdAndDecision(caseId, ComparisonDecision.MATCH);
            long noMatchComparisons = faceComparisonRepository.countByCaseIdAndDecision(caseId, ComparisonDecision.NO_MATCH);
            long uncertainComparisons = faceComparisonRepository.countByCaseIdAndDecision(caseId, ComparisonDecision.UNCERTAIN);

            long highConfidenceComparisons = faceComparisonRepository.countByCaseIdAndConfidenceLevel(caseId, ConfidenceLevel.HIGH);
            long veryHighConfidenceComparisons = faceComparisonRepository.countByCaseIdAndConfidenceLevel(caseId, ConfidenceLevel.VERY_HIGH);

            return new FaceComparisonStatistics(
                totalComparisons, matchComparisons, noMatchComparisons, uncertainComparisons,
                highConfidenceComparisons, veryHighConfidenceComparisons
            );

        } catch (Exception e) {
            logger.error("Failed to get face comparison statistics for case {}: {}", caseId, e.getMessage());
            throw new RuntimeException("Failed to get face comparison statistics", e);
        }
    }

    /**
     * Extract analysis results from AI service response
     * 
     * @param faceComparison Face comparison entity
     * @param analysisResult AI service response
     */
    private void extractAnalysisResults(FaceComparison faceComparison, JsonNode analysisResult) {
        try {
            // Extract basic results
            if (analysisResult.has("match_score")) {
                faceComparison.setMatchScore(analysisResult.get("match_score").asDouble());
            }

            if (analysisResult.has("decision")) {
                String decisionStr = analysisResult.get("decision").asText();
                faceComparison.setDecision(ComparisonDecision.valueOf(decisionStr));
            }

            if (analysisResult.has("confidence_level")) {
                String confidenceStr = analysisResult.get("confidence_level").asText();
                faceComparison.setConfidenceLevel(ConfidenceLevel.valueOf(confidenceStr));
            }

            if (analysisResult.has("model_version")) {
                faceComparison.setModelVersion(analysisResult.get("model_version").asText());
            }

            if (analysisResult.has("processing_time_ms")) {
                faceComparison.setProcessingTimeMs(analysisResult.get("processing_time_ms").asInt());
            }

            // Store full analysis result as JSON
            faceComparison.setAnalysisResult(analysisResult.toString());

        } catch (Exception e) {
            logger.error("Failed to extract analysis results: {}", e.getMessage());
            throw new RuntimeException("Failed to extract analysis results", e);
        }
    }

    /**
     * Face comparison statistics data class
     */
    public static class FaceComparisonStatistics {
        private final long totalComparisons;
        private final long matchComparisons;
        private final long noMatchComparisons;
        private final long uncertainComparisons;
        private final long highConfidenceComparisons;
        private final long veryHighConfidenceComparisons;

        public FaceComparisonStatistics(long totalComparisons, long matchComparisons, 
                                      long noMatchComparisons, long uncertainComparisons,
                                      long highConfidenceComparisons, long veryHighConfidenceComparisons) {
            this.totalComparisons = totalComparisons;
            this.matchComparisons = matchComparisons;
            this.noMatchComparisons = noMatchComparisons;
            this.uncertainComparisons = uncertainComparisons;
            this.highConfidenceComparisons = highConfidenceComparisons;
            this.veryHighConfidenceComparisons = veryHighConfidenceComparisons;
        }

        // Getters
        public long getTotalComparisons() { return totalComparisons; }
        public long getMatchComparisons() { return matchComparisons; }
        public long getNoMatchComparisons() { return noMatchComparisons; }
        public long getUncertainComparisons() { return uncertainComparisons; }
        public long getHighConfidenceComparisons() { return highConfidenceComparisons; }
        public long getVeryHighConfidenceComparisons() { return veryHighConfidenceComparisons; }
    }
}
