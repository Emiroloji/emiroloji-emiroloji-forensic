package com.forensic.case.service;

import com.forensic.case.dto.queue.BatchAnalysisRequest;
import com.forensic.case.dto.queue.BatchAnalysisResponse;
import com.forensic.case.dto.queue.ProgressUpdate;
import com.forensic.case.entity.Case;
import com.forensic.case.entity.FaceComparison;
import com.forensic.case.repository.CaseRepository;
import com.forensic.case.repository.FaceComparisonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class BatchAnalysisService {

    @Autowired
    private CaseRepository caseRepository;

    @Autowired
    private FaceComparisonRepository faceComparisonRepository;

    @Autowired
    private ProgressTrackingService progressTrackingService;

    @Autowired
    private RestTemplate restTemplate;

    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    public BatchAnalysisResponse processBatchAnalysis(BatchAnalysisRequest request) {
        String batchId = request.getBatchId();
        logger.info("Starting batch analysis for batch: {}", batchId);

        try {
            // Get case information
            Case caseEntity = caseRepository.findById(request.getCaseId())
                    .orElseThrow(() -> new RuntimeException("Case not found"));

            // Process each comparison in the batch
            List<FaceComparison> results = new ArrayList<>();
            int totalComparisons = request.getComparisons().size();
            int completedComparisons = 0;

            for (BatchAnalysisRequest.ComparisonRequest comparisonRequest : request.getComparisons()) {
                try {
                    // Update progress
                    int progress = (completedComparisons * 100) / totalComparisons;
                    progressTrackingService.updateProgress(batchId, progress, 
                        "Processing comparison " + (completedComparisons + 1) + " of " + totalComparisons, "PROCESSING");

                    // Process individual comparison
                    FaceComparison comparison = processIndividualComparison(comparisonRequest, caseEntity);
                    results.add(comparison);

                    completedComparisons++;

                } catch (Exception e) {
                    logger.error("Error processing comparison in batch {}: {}", batchId, e.getMessage());
                    // Continue with next comparison
                }
            }

            // Update final progress
            progressTrackingService.updateProgress(batchId, 100, "All comparisons completed", "COMPLETED");

            return BatchAnalysisResponse.builder()
                    .batchId(batchId)
                    .caseId(request.getCaseId())
                    .status("COMPLETED")
                    .totalComparisons(totalComparisons)
                    .successfulComparisons(results.size())
                    .failedComparisons(totalComparisons - results.size())
                    .results(results)
                    .completedAt(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            logger.error("Error in batch analysis: {}", batchId, e);
            throw new RuntimeException("Batch analysis failed: " + e.getMessage(), e);
        }
    }

    private FaceComparison processIndividualComparison(BatchAnalysisRequest.ComparisonRequest comparisonRequest, Case caseEntity) {
        try {
            // Call AI service for face comparison
            String aiServiceUrl = "http://ai-service:8000/compare-faces";
            
            // Prepare request for AI service
            // This would typically involve sending the image data to the AI service
            // For now, we'll create a mock comparison result
            
            FaceComparison comparison = new FaceComparison();
            comparison.setCaseId(caseEntity.getId());
            comparison.setImage1Id(comparisonRequest.getImage1Id());
            comparison.setImage2Id(comparisonRequest.getImage2Id());
            comparison.setSimilarityScore(0.85); // Mock score
            comparison.setIsMatch(true); // Mock result
            comparison.setConfidence(0.92); // Mock confidence
            comparison.setCreatedAt(LocalDateTime.now());
            comparison.setCreatedBy(comparisonRequest.getUserId());

            // Save to database
            return faceComparisonRepository.save(comparison);

        } catch (Exception e) {
            logger.error("Error processing individual comparison: {}", e.getMessage());
            throw new RuntimeException("Individual comparison failed: " + e.getMessage(), e);
        }
    }

    public void cancelBatchAnalysis(String batchId) {
        logger.info("Cancelling batch analysis: {}", batchId);
        
        // In a real implementation, you would:
        // 1. Stop any running tasks for this batch
        // 2. Clean up resources
        // 3. Update the batch status to cancelled
        
        // For now, just log the cancellation
        logger.info("Batch analysis cancelled: {}", batchId);
    }

    public BatchAnalysisResponse getBatchAnalysisResult(String batchId) {
        // In a real implementation, you would retrieve the result from storage
        // For now, return a placeholder
        return BatchAnalysisResponse.builder()
                .batchId(batchId)
                .status("COMPLETED")
                .build();
    }

    public List<BatchAnalysisResponse> getBatchAnalysisHistory(String userId) {
        // In a real implementation, you would retrieve the history from storage
        // For now, return an empty list
        return new ArrayList<>();
    }
}
