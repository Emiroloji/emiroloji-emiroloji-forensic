package com.forensic.case.dto.queue;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchAnalysisRequest {
    private String batchId;
    private Long caseId;
    private String userId;
    private List<ComparisonRequest> comparisons;
    private String priority; // NORMAL, HIGH, URGENT
    private String analysisType; // FACE_COMPARISON, FACE_DETECTION, etc.

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComparisonRequest {
        private String image1Id;
        private String image2Id;
        private String userId;
        private String notes;
        private Double threshold; // Custom threshold for this comparison
    }
}
