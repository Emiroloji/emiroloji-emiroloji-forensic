package com.forensic.case.repository;

import com.forensic.case.entity.FaceComparison;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Face Comparison Repository Interface
 * 
 * Provides data access methods for FaceComparison entities
 */
@Repository
public interface FaceComparisonRepository extends JpaRepository<FaceComparison, UUID> {

    /**
     * Find face comparisons by case ID
     */
    Page<FaceComparison> findByCaseId(UUID caseId, Pageable pageable);

    /**
     * Find face comparisons by analyzer
     */
    Page<FaceComparison> findByAnalyzedBy(UUID analyzedBy, Pageable pageable);

    /**
     * Find face comparisons by decision
     */
    Page<FaceComparison> findByDecision(ComparisonDecision decision, Pageable pageable);

    /**
     * Find face comparisons by confidence level
     */
    Page<FaceComparison> findByConfidenceLevel(ConfidenceLevel confidenceLevel, Pageable pageable);

    /**
     * Find face comparisons by model version
     */
    Page<FaceComparison> findByModelVersion(String modelVersion, Pageable pageable);

    /**
     * Find face comparisons by comparison name
     */
    Page<FaceComparison> findByComparisonNameContainingIgnoreCase(String comparisonName, Pageable pageable);

    /**
     * Find face comparisons by image 1 ID
     */
    Page<FaceComparison> findByImage1Id(UUID image1Id, Pageable pageable);

    /**
     * Find face comparisons by image 2 ID
     */
    Page<FaceComparison> findByImage2Id(UUID image2Id, Pageable pageable);

    /**
     * Find face comparisons by image ID (either image1 or image2)
     */
    @Query("SELECT fc FROM FaceComparison fc WHERE fc.image1Id = :imageId OR fc.image2Id = :imageId")
    Page<FaceComparison> findByImageId(@Param("imageId") UUID imageId, Pageable pageable);

    /**
     * Find face comparisons by case ID and decision
     */
    @Query("SELECT fc FROM FaceComparison fc WHERE fc.caseId = :caseId AND fc.decision = :decision")
    Page<FaceComparison> findByCaseIdAndDecision(@Param("caseId") UUID caseId, @Param("decision") ComparisonDecision decision, Pageable pageable);

    /**
     * Find face comparisons by case ID and confidence level
     */
    @Query("SELECT fc FROM FaceComparison fc WHERE fc.caseId = :caseId AND fc.confidenceLevel = :confidenceLevel")
    Page<FaceComparison> findByCaseIdAndConfidenceLevel(@Param("caseId") UUID caseId, @Param("confidenceLevel") ConfidenceLevel confidenceLevel, Pageable pageable);

    /**
     * Find face comparisons by analyzer and decision
     */
    @Query("SELECT fc FROM FaceComparison fc WHERE fc.analyzedBy = :analyzedBy AND fc.decision = :decision")
    Page<FaceComparison> findByAnalyzedByAndDecision(@Param("analyzedBy") UUID analyzedBy, @Param("decision") ComparisonDecision decision, Pageable pageable);

    /**
     * Find face comparisons by analyzer and confidence level
     */
    @Query("SELECT fc FROM FaceComparison fc WHERE fc.analyzedBy = :analyzedBy AND fc.confidenceLevel = :confidenceLevel")
    Page<FaceComparison> findByAnalyzedByAndConfidenceLevel(@Param("analyzedBy") UUID analyzedBy, @Param("confidenceLevel") ConfidenceLevel confidenceLevel, Pageable pageable);

    /**
     * Find face comparisons by decision and confidence level
     */
    @Query("SELECT fc FROM FaceComparison fc WHERE fc.decision = :decision AND fc.confidenceLevel = :confidenceLevel")
    Page<FaceComparison> findByDecisionAndConfidenceLevel(@Param("decision") ComparisonDecision decision, @Param("confidenceLevel") ConfidenceLevel confidenceLevel, Pageable pageable);

    /**
     * Find face comparisons by match score range
     */
    @Query("SELECT fc FROM FaceComparison fc WHERE fc.matchScore BETWEEN :minScore AND :maxScore")
    Page<FaceComparison> findByMatchScoreBetween(@Param("minScore") Double minScore, @Param("maxScore") Double maxScore, Pageable pageable);

    /**
     * Find face comparisons by processing time range
     */
    @Query("SELECT fc FROM FaceComparison fc WHERE fc.processingTimeMs BETWEEN :minTime AND :maxTime")
    Page<FaceComparison> findByProcessingTimeBetween(@Param("minTime") Integer minTime, @Param("maxTime") Integer maxTime, Pageable pageable);

    /**
     * Find face comparisons analyzed after a specific date
     */
    Page<FaceComparison> findByAnalyzedAtAfter(LocalDateTime date, Pageable pageable);

    /**
     * Find face comparisons analyzed before a specific date
     */
    Page<FaceComparison> findByAnalyzedAtBefore(LocalDateTime date, Pageable pageable);

    /**
     * Find face comparisons analyzed between dates
     */
    Page<FaceComparison> findByAnalyzedAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    /**
     * Find face comparisons by multiple criteria
     */
    @Query("SELECT fc FROM FaceComparison fc WHERE " +
           "(:caseId IS NULL OR fc.caseId = :caseId) AND " +
           "(:analyzedBy IS NULL OR fc.analyzedBy = :analyzedBy) AND " +
           "(:decision IS NULL OR fc.decision = :decision) AND " +
           "(:confidenceLevel IS NULL OR fc.confidenceLevel = :confidenceLevel) AND " +
           "(:modelVersion IS NULL OR fc.modelVersion = :modelVersion)")
    Page<FaceComparison> findByMultipleCriteria(@Param("caseId") UUID caseId,
                                              @Param("analyzedBy") UUID analyzedBy,
                                              @Param("decision") ComparisonDecision decision,
                                              @Param("confidenceLevel") ConfidenceLevel confidenceLevel,
                                              @Param("modelVersion") String modelVersion,
                                              Pageable pageable);

    /**
     * Find face comparisons with high match scores
     */
    @Query("SELECT fc FROM FaceComparison fc WHERE fc.matchScore >= :threshold ORDER BY fc.matchScore DESC")
    Page<FaceComparison> findHighMatchScoreComparisons(@Param("threshold") Double threshold, Pageable pageable);

    /**
     * Find face comparisons with low match scores
     */
    @Query("SELECT fc FROM FaceComparison fc WHERE fc.matchScore <= :threshold ORDER BY fc.matchScore ASC")
    Page<FaceComparison> findLowMatchScoreComparisons(@Param("threshold") Double threshold, Pageable pageable);

    /**
     * Find face comparisons with fast processing times
     */
    @Query("SELECT fc FROM FaceComparison fc WHERE fc.processingTimeMs <= :maxTime ORDER BY fc.processingTimeMs ASC")
    Page<FaceComparison> findFastProcessingComparisons(@Param("maxTime") Integer maxTime, Pageable pageable);

    /**
     * Find face comparisons with slow processing times
     */
    @Query("SELECT fc FROM FaceComparison fc WHERE fc.processingTimeMs >= :minTime ORDER BY fc.processingTimeMs DESC")
    Page<FaceComparison> findSlowProcessingComparisons(@Param("minTime") Integer minTime, Pageable pageable);

    /**
     * Find face comparisons with most recent analysis
     */
    @Query("SELECT fc FROM FaceComparison fc ORDER BY fc.analyzedAt DESC")
    Page<FaceComparison> findMostRecentComparisons(Pageable pageable);

    /**
     * Find face comparisons with oldest analysis
     */
    @Query("SELECT fc FROM FaceComparison fc ORDER BY fc.analyzedAt ASC")
    Page<FaceComparison> findOldestComparisons(Pageable pageable);

    /**
     * Find face comparisons by case ID and image ID
     */
    @Query("SELECT fc FROM FaceComparison fc WHERE fc.caseId = :caseId AND (fc.image1Id = :imageId OR fc.image2Id = :imageId)")
    Page<FaceComparison> findByCaseIdAndImageId(@Param("caseId") UUID caseId, @Param("imageId") UUID imageId, Pageable pageable);

    /**
     * Find face comparisons by case ID and analyzer
     */
    @Query("SELECT fc FROM FaceComparison fc WHERE fc.caseId = :caseId AND fc.analyzedBy = :analyzedBy")
    Page<FaceComparison> findByCaseIdAndAnalyzedBy(@Param("caseId") UUID caseId, @Param("analyzedBy") UUID analyzedBy, Pageable pageable);

    /**
     * Find face comparisons by case ID and model version
     */
    @Query("SELECT fc FROM FaceComparison fc WHERE fc.caseId = :caseId AND fc.modelVersion = :modelVersion")
    Page<FaceComparison> findByCaseIdAndModelVersion(@Param("caseId") UUID caseId, @Param("modelVersion") String modelVersion, Pageable pageable);

    /**
     * Count face comparisons by case ID
     */
    long countByCaseId(UUID caseId);

    /**
     * Count face comparisons by analyzer
     */
    long countByAnalyzedBy(UUID analyzedBy);

    /**
     * Count face comparisons by decision
     */
    long countByDecision(ComparisonDecision decision);

    /**
     * Count face comparisons by confidence level
     */
    long countByConfidenceLevel(ConfidenceLevel confidenceLevel);

    /**
     * Count face comparisons by model version
     */
    long countByModelVersion(String modelVersion);

    /**
     * Count face comparisons by case ID and decision
     */
    @Query("SELECT COUNT(fc) FROM FaceComparison fc WHERE fc.caseId = :caseId AND fc.decision = :decision")
    long countByCaseIdAndDecision(@Param("caseId") UUID caseId, @Param("decision") ComparisonDecision decision);

    /**
     * Count face comparisons by case ID and confidence level
     */
    @Query("SELECT COUNT(fc) FROM FaceComparison fc WHERE fc.caseId = :caseId AND fc.confidenceLevel = :confidenceLevel")
    long countByCaseIdAndConfidenceLevel(@Param("caseId") UUID caseId, @Param("confidenceLevel") ConfidenceLevel confidenceLevel);

    /**
     * Count face comparisons by analyzer and decision
     */
    @Query("SELECT COUNT(fc) FROM FaceComparison fc WHERE fc.analyzedBy = :analyzedBy AND fc.decision = :decision")
    long countByAnalyzedByAndDecision(@Param("analyzedBy") UUID analyzedBy, @Param("decision") ComparisonDecision decision);

    /**
     * Count face comparisons by analyzer and confidence level
     */
    @Query("SELECT COUNT(fc) FROM FaceComparison fc WHERE fc.analyzedBy = :analyzedBy AND fc.confidenceLevel = :confidenceLevel")
    long countByAnalyzedByAndConfidenceLevel(@Param("analyzedBy") UUID analyzedBy, @Param("confidenceLevel") ConfidenceLevel confidenceLevel);

    /**
     * Count face comparisons by decision and confidence level
     */
    @Query("SELECT COUNT(fc) FROM FaceComparison fc WHERE fc.decision = :decision AND fc.confidenceLevel = :confidenceLevel")
    long countByDecisionAndConfidenceLevel(@Param("decision") ComparisonDecision decision, @Param("confidenceLevel") ConfidenceLevel confidenceLevel);

    /**
     * Count face comparisons by match score range
     */
    @Query("SELECT COUNT(fc) FROM FaceComparison fc WHERE fc.matchScore BETWEEN :minScore AND :maxScore")
    long countByMatchScoreBetween(@Param("minScore") Double minScore, @Param("maxScore") Double maxScore);

    /**
     * Count face comparisons by processing time range
     */
    @Query("SELECT COUNT(fc) FROM FaceComparison fc WHERE fc.processingTimeMs BETWEEN :minTime AND :maxTime")
    long countByProcessingTimeBetween(@Param("minTime") Integer minTime, @Param("maxTime") Integer maxTime);

    /**
     * Count face comparisons analyzed after a specific date
     */
    long countByAnalyzedAtAfter(LocalDateTime date);

    /**
     * Count face comparisons analyzed before a specific date
     */
    long countByAnalyzedAtBefore(LocalDateTime date);

    /**
     * Count face comparisons analyzed between dates
     */
    long countByAnalyzedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Count face comparisons with high match scores
     */
    @Query("SELECT COUNT(fc) FROM FaceComparison fc WHERE fc.matchScore >= :threshold")
    long countHighMatchScoreComparisons(@Param("threshold") Double threshold);

    /**
     * Count face comparisons with low match scores
     */
    @Query("SELECT COUNT(fc) FROM FaceComparison fc WHERE fc.matchScore <= :threshold")
    long countLowMatchScoreComparisons(@Param("threshold") Double threshold);

    /**
     * Count face comparisons with fast processing times
     */
    @Query("SELECT COUNT(fc) FROM FaceComparison fc WHERE fc.processingTimeMs <= :maxTime")
    long countFastProcessingComparisons(@Param("maxTime") Integer maxTime);

    /**
     * Count face comparisons with slow processing times
     */
    @Query("SELECT COUNT(fc) FROM FaceComparison fc WHERE fc.processingTimeMs >= :minTime")
    long countSlowProcessingComparisons(@Param("minTime") Integer minTime);

    /**
     * Count face comparisons by case ID and analyzer
     */
    @Query("SELECT COUNT(fc) FROM FaceComparison fc WHERE fc.caseId = :caseId AND fc.analyzedBy = :analyzedBy")
    long countByCaseIdAndAnalyzedBy(@Param("caseId") UUID caseId, @Param("analyzedBy") UUID analyzedBy);

    /**
     * Count face comparisons by case ID and model version
     */
    @Query("SELECT COUNT(fc) FROM FaceComparison fc WHERE fc.caseId = :caseId AND fc.modelVersion = :modelVersion")
    long countByCaseIdAndModelVersion(@Param("caseId") UUID caseId, @Param("modelVersion") String modelVersion);

    /**
     * Count face comparisons by image ID
     */
    @Query("SELECT COUNT(fc) FROM FaceComparison fc WHERE fc.image1Id = :imageId OR fc.image2Id = :imageId")
    long countByImageId(@Param("imageId") UUID imageId);

    /**
     * Count face comparisons by case ID and image ID
     */
    @Query("SELECT COUNT(fc) FROM FaceComparison fc WHERE fc.caseId = :caseId AND (fc.image1Id = :imageId OR fc.image2Id = :imageId)")
    long countByCaseIdAndImageId(@Param("caseId") UUID caseId, @Param("imageId") UUID imageId);
}
