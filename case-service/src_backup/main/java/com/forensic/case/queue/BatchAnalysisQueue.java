package com.forensic.case.queue;

import com.forensic.case.dto.queue.BatchAnalysisRequest;
import com.forensic.case.dto.queue.BatchAnalysisResponse;
import com.forensic.case.dto.queue.ProgressUpdate;
import com.forensic.case.service.BatchAnalysisService;
import com.forensic.case.service.ProgressTrackingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class BatchAnalysisQueue {

    private static final Logger logger = LoggerFactory.getLogger(BatchAnalysisQueue.class);

    @Autowired
    private BatchAnalysisService batchAnalysisService;

    @Autowired
    private ProgressTrackingService progressTrackingService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = "batch.analysis.queue")
    public void processBatchAnalysis(BatchAnalysisRequest request) {
        String batchId = request.getBatchId();
        logger.info("Processing batch analysis request: {}", batchId);

        try {
            // Update progress to started
            progressTrackingService.updateProgress(batchId, 0, "Batch analysis started", "STARTED");

            // Process the batch analysis
            BatchAnalysisResponse response = batchAnalysisService.processBatchAnalysis(request);

            // Update progress to completed
            progressTrackingService.updateProgress(batchId, 100, "Batch analysis completed", "COMPLETED");

            // Send response to result queue
            rabbitTemplate.convertAndSend("batch.analysis.result.queue", response);

            logger.info("Batch analysis completed successfully: {}", batchId);

        } catch (Exception e) {
            logger.error("Error processing batch analysis: {}", batchId, e);
            
            // Update progress to failed
            progressTrackingService.updateProgress(batchId, -1, "Batch analysis failed: " + e.getMessage(), "FAILED");

            // Send error response
            BatchAnalysisResponse errorResponse = BatchAnalysisResponse.builder()
                    .batchId(batchId)
                    .status("FAILED")
                    .errorMessage(e.getMessage())
                    .build();

            rabbitTemplate.convertAndSend("batch.analysis.result.queue", errorResponse);
        }
    }

    @RabbitListener(queues = "batch.analysis.priority.queue")
    public void processPriorityBatchAnalysis(BatchAnalysisRequest request) {
        String batchId = request.getBatchId();
        logger.info("Processing priority batch analysis request: {}", batchId);

        try {
            // Update progress to started
            progressTrackingService.updateProgress(batchId, 0, "Priority batch analysis started", "STARTED");

            // Process the batch analysis with higher priority
            BatchAnalysisResponse response = batchAnalysisService.processBatchAnalysis(request);

            // Update progress to completed
            progressTrackingService.updateProgress(batchId, 100, "Priority batch analysis completed", "COMPLETED");

            // Send response to result queue
            rabbitTemplate.convertAndSend("batch.analysis.result.queue", response);

            logger.info("Priority batch analysis completed successfully: {}", batchId);

        } catch (Exception e) {
            logger.error("Error processing priority batch analysis: {}", batchId, e);
            
            // Update progress to failed
            progressTrackingService.updateProgress(batchId, -1, "Priority batch analysis failed: " + e.getMessage(), "FAILED");

            // Send error response
            BatchAnalysisResponse errorResponse = BatchAnalysisResponse.builder()
                    .batchId(batchId)
                    .status("FAILED")
                    .errorMessage(e.getMessage())
                    .build();

            rabbitTemplate.convertAndSend("batch.analysis.result.queue", errorResponse);
        }
    }

    @RabbitListener(queues = "batch.analysis.cancel.queue")
    public void cancelBatchAnalysis(String batchId) {
        logger.info("Cancelling batch analysis: {}", batchId);

        try {
            // Cancel the batch analysis
            batchAnalysisService.cancelBatchAnalysis(batchId);

            // Update progress to cancelled
            progressTrackingService.updateProgress(batchId, -1, "Batch analysis cancelled", "CANCELLED");

            logger.info("Batch analysis cancelled successfully: {}", batchId);

        } catch (Exception e) {
            logger.error("Error cancelling batch analysis: {}", batchId, e);
        }
    }
}
