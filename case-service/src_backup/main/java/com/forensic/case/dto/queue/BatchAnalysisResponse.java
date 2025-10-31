package com.forensic.case.dto.queue;

import com.forensic.case.entity.FaceComparison;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchAnalysisResponse {
    private String batchId;
    private Long caseId;
    private String status; // STARTED, PROCESSING, COMPLETED, FAILED, CANCELLED
    private int totalComparisons;
    private int successfulComparisons;
    private int failedComparisons;
    private List<FaceComparison> results;
    private String errorMessage;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private long processingTimeMs;
}
