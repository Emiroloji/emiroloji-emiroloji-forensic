package com.forensic.case.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Face Comparison entity for storing face comparison analysis results
 */
@Entity
@Table(name = "face_comparisons", schema = "cases")
@EntityListeners(AuditingEntityListener.class)
public class FaceComparison {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @NotNull
    @Column(name = "case_id", nullable = false)
    private UUID caseId;
    
    @Column(name = "comparison_name")
    private String comparisonName;
    
    @NotNull
    @Column(name = "image1_id", nullable = false)
    private UUID image1Id;
    
    @NotNull
    @Column(name = "image2_id", nullable = false)
    private UUID image2Id;
    
    @Column(name = "analysis_result", columnDefinition = "jsonb")
    private String analysisResult; // JSON string containing detailed analysis
    
    @Column(name = "match_score")
    private Double matchScore;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "decision")
    private ComparisonDecision decision;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "confidence_level")
    private ConfidenceLevel confidenceLevel;
    
    @Column(name = "model_version")
    private String modelVersion;
    
    @Column(name = "processing_time_ms")
    private Integer processingTimeMs;
    
    @Column(name = "analyzed_by")
    private UUID analyzedBy;
    
    @CreatedDate
    @Column(name = "analyzed_at", nullable = false, updatable = false)
    private LocalDateTime analyzedAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructors
    public FaceComparison() {}
    
    public FaceComparison(UUID caseId, UUID image1Id, UUID image2Id, UUID analyzedBy) {
        this.caseId = caseId;
        this.image1Id = image1Id;
        this.image2Id = image2Id;
        this.analyzedBy = analyzedBy;
    }
    
    // Getters and Setters
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public UUID getCaseId() {
        return caseId;
    }
    
    public void setCaseId(UUID caseId) {
        this.caseId = caseId;
    }
    
    public String getComparisonName() {
        return comparisonName;
    }
    
    public void setComparisonName(String comparisonName) {
        this.comparisonName = comparisonName;
    }
    
    public UUID getImage1Id() {
        return image1Id;
    }
    
    public void setImage1Id(UUID image1Id) {
        this.image1Id = image1Id;
    }
    
    public UUID getImage2Id() {
        return image2Id;
    }
    
    public void setImage2Id(UUID image2Id) {
        this.image2Id = image2Id;
    }
    
    public String getAnalysisResult() {
        return analysisResult;
    }
    
    public void setAnalysisResult(String analysisResult) {
        this.analysisResult = analysisResult;
    }
    
    public Double getMatchScore() {
        return matchScore;
    }
    
    public void setMatchScore(Double matchScore) {
        this.matchScore = matchScore;
    }
    
    public ComparisonDecision getDecision() {
        return decision;
    }
    
    public void setDecision(ComparisonDecision decision) {
        this.decision = decision;
    }
    
    public ConfidenceLevel getConfidenceLevel() {
        return confidenceLevel;
    }
    
    public void setConfidenceLevel(ConfidenceLevel confidenceLevel) {
        this.confidenceLevel = confidenceLevel;
    }
    
    public String getModelVersion() {
        return modelVersion;
    }
    
    public void setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
    }
    
    public Integer getProcessingTimeMs() {
        return processingTimeMs;
    }
    
    public void setProcessingTimeMs(Integer processingTimeMs) {
        this.processingTimeMs = processingTimeMs;
    }
    
    public UUID getAnalyzedBy() {
        return analyzedBy;
    }
    
    public void setAnalyzedBy(UUID analyzedBy) {
        this.analyzedBy = analyzedBy;
    }
    
    public LocalDateTime getAnalyzedAt() {
        return analyzedAt;
    }
    
    public void setAnalyzedAt(LocalDateTime analyzedAt) {
        this.analyzedAt = analyzedAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    // Business methods
    public boolean isMatch() {
        return decision == ComparisonDecision.MATCH;
    }
    
    public boolean isNoMatch() {
        return decision == ComparisonDecision.NO_MATCH;
    }
    
    public boolean isUncertain() {
        return decision == ComparisonDecision.UNCERTAIN;
    }
    
    public boolean isHighConfidence() {
        return confidenceLevel == ConfidenceLevel.HIGH || confidenceLevel == ConfidenceLevel.VERY_HIGH;
    }
    
    public boolean isLowConfidence() {
        return confidenceLevel == ConfidenceLevel.LOW || confidenceLevel == ConfidenceLevel.VERY_LOW;
    }
    
    public String getFormattedProcessingTime() {
        if (processingTimeMs == null) {
            return "N/A";
        }
        
        if (processingTimeMs < 1000) {
            return processingTimeMs + " ms";
        } else {
            return String.format("%.2f s", processingTimeMs / 1000.0);
        }
    }
}

/**
 * Comparison decision enumeration
 */
enum ComparisonDecision {
    MATCH("Match"),
    NO_MATCH("No Match"),
    UNCERTAIN("Uncertain");
    
    private final String displayName;
    
    ComparisonDecision(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}

/**
 * Confidence level enumeration
 */
enum ConfidenceLevel {
    VERY_HIGH("Very High"),
    HIGH("High"),
    MEDIUM("Medium"),
    LOW("Low"),
    VERY_LOW("Very Low");
    
    private final String displayName;
    
    ConfidenceLevel(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
