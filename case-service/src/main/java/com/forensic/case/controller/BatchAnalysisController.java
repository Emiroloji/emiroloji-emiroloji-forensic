package com.forensic.case.controller;

import com.forensic.case.dto.queue.BatchAnalysisRequest;
import com.forensic.case.dto.queue.BatchAnalysisResponse;
import com.forensic.case.dto.queue.ProgressUpdate;
import com.forensic.case.service.BatchAnalysisService;
import com.forensic.case.service.ProgressTrackingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/batch-analysis")
public class BatchAnalysisController {

    @Autowired
    private BatchAnalysisService batchAnalysisService;

    @Autowired
    private ProgressTrackingService progressTrackingService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @PostMapping("/submit")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<BatchAnalysisResponse> submitBatchAnalysis(@Valid @RequestBody BatchAnalysisRequest request) {
        try {
            // Generate batch ID if not provided
            if (request.getBatchId() == null || request.getBatchId().isEmpty()) {
                request.setBatchId(UUID.randomUUID().toString());
            }

            // Determine queue based on priority
            String queueName = "HIGH".equals(request.getPriority()) || "URGENT".equals(request.getPriority()) 
                ? "batch.analysis.priority.queue" 
                : "batch.analysis.queue";

            // Send to queue
            rabbitTemplate.convertAndSend(queueName, request);

            // Return initial response
            BatchAnalysisResponse response = BatchAnalysisResponse.builder()
                    .batchId(request.getBatchId())
                    .caseId(request.getCaseId())
                    .status("SUBMITTED")
                    .totalComparisons(request.getComparisons().size())
                    .build();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/progress/{batchId}")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<ProgressUpdate> getProgress(@PathVariable String batchId) {
        try {
            ProgressUpdate progress = progressTrackingService.getProgress(batchId);
            if (progress != null) {
                return ResponseEntity.ok(progress);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/result/{batchId}")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<BatchAnalysisResponse> getResult(@PathVariable String batchId) {
        try {
            BatchAnalysisResponse result = batchAnalysisService.getBatchAnalysisResult(batchId);
            if (result != null) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/cancel/{batchId}")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<Void> cancelBatchAnalysis(@PathVariable String batchId) {
        try {
            // Send cancellation request to queue
            rabbitTemplate.convertAndSend("batch.analysis.cancel.queue", batchId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/history")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<List<BatchAnalysisResponse>> getBatchAnalysisHistory(@RequestParam String userId) {
        try {
            List<BatchAnalysisResponse> history = batchAnalysisService.getBatchAnalysisHistory(userId);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/active")
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, ProgressUpdate>> getActiveBatchAnalyses() {
        try {
            Map<String, ProgressUpdate> activeAnalyses = progressTrackingService.getAllProgress();
            return ResponseEntity.ok(activeAnalyses);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/cleanup")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> cleanupCompletedAnalyses() {
        try {
            progressTrackingService.clearCompletedProgress();
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
}
