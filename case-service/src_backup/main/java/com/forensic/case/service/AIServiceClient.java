package com.forensic.case.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

/**
 * AI Service Client for communicating with the AI service
 * 
 * This service handles:
 * - Face comparison requests
 * - Video processing requests
 * - Batch analysis requests
 * - Report generation requests
 */
@Service
public class AIServiceClient {

    private static final Logger logger = LoggerFactory.getLogger(AIServiceClient.class);

    @Autowired
    private WebClient.Builder webClientBuilder;

    @Value("${ai.service.url:http://ai-service:8000}")
    private String aiServiceUrl;

    @Value("${ai.service.timeout:30000}")
    private int timeoutMs;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Compare two face images
     * 
     * @param image1Id First image ID
     * @param image2Id Second image ID
     * @param threshold Comparison threshold
     * @param authToken Authentication token
     * @return Comparison result as JSON
     */
    public Mono<JsonNode> compareFaces(UUID image1Id, UUID image2Id, Double threshold, String authToken) {
        try {
            Map<String, Object> requestBody = Map.of(
                "image1_id", image1Id.toString(),
                "image2_id", image2Id.toString(),
                "threshold", threshold != null ? threshold : 0.75,
                "return_confidence_interval", true
            );

            return webClientBuilder.build()
                .post()
                .uri(aiServiceUrl + "/api/ai/compare-faces")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .timeout(Duration.ofMillis(timeoutMs))
                .doOnSuccess(result -> logger.info("Face comparison completed for images {} and {}", image1Id, image2Id))
                .doOnError(error -> logger.error("Face comparison failed: {}", error.getMessage()));

        } catch (Exception e) {
            logger.error("Failed to create face comparison request: {}", e.getMessage());
            return Mono.error(new RuntimeException("Face comparison request failed", e));
        }
    }

    /**
     * Process video for face detection
     * 
     * @param videoId Video ID
     * @param frameExtractionRate Frames per second to extract
     * @param qualityThreshold Minimum face quality threshold
     * @param maxFrames Maximum number of frames to process
     * @param authToken Authentication token
     * @return Video processing result as JSON
     */
    public Mono<JsonNode> processVideo(UUID videoId, Integer frameExtractionRate, Double qualityThreshold, 
                                       Integer maxFrames, String authToken) {
        try {
            Map<String, Object> requestBody = Map.of(
                "video_id", videoId.toString(),
                "frame_extraction_rate", frameExtractionRate != null ? frameExtractionRate : 1,
                "quality_threshold", qualityThreshold != null ? qualityThreshold : 0.7,
                "max_frames", maxFrames != null ? maxFrames : 100
            );

            return webClientBuilder.build()
                .post()
                .uri(aiServiceUrl + "/api/ai/process-video")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .timeout(Duration.ofMillis(timeoutMs * 2)) // Longer timeout for video processing
                .doOnSuccess(result -> logger.info("Video processing completed for video {}", videoId))
                .doOnError(error -> logger.error("Video processing failed: {}", error.getMessage()));

        } catch (Exception e) {
            logger.error("Failed to create video processing request: {}", e.getMessage());
            return Mono.error(new RuntimeException("Video processing request failed", e));
        }
    }

    /**
     * Perform batch face comparison
     * 
     * @param referenceImageId Reference image ID
     * @param candidateImageIds List of candidate image IDs
     * @param threshold Comparison threshold
     * @param authToken Authentication token
     * @return Batch comparison result as JSON
     */
    public Mono<JsonNode> batchCompareFaces(UUID referenceImageId, java.util.List<UUID> candidateImageIds, 
                                           Double threshold, String authToken) {
        try {
            Map<String, Object> requestBody = Map.of(
                "reference_image_id", referenceImageId.toString(),
                "candidate_image_ids", candidateImageIds.stream().map(UUID::toString).toList(),
                "threshold", threshold != null ? threshold : 0.75
            );

            return webClientBuilder.build()
                .post()
                .uri(aiServiceUrl + "/api/ai/batch-compare")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .timeout(Duration.ofMillis(timeoutMs * 3)) // Longer timeout for batch processing
                .doOnSuccess(result -> logger.info("Batch comparison completed for {} candidates", candidateImageIds.size()))
                .doOnError(error -> logger.error("Batch comparison failed: {}", error.getMessage()));

        } catch (Exception e) {
            logger.error("Failed to create batch comparison request: {}", e.getMessage());
            return Mono.error(new RuntimeException("Batch comparison request failed", e));
        }
    }

    /**
     * Generate scientific report
     * 
     * @param comparisonId Comparison ID
     * @param authToken Authentication token
     * @return Report data as JSON
     */
    public Mono<JsonNode> generateReport(UUID comparisonId, String authToken) {
        try {
            return webClientBuilder.build()
                .post()
                .uri(aiServiceUrl + "/api/ai/generate-report")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(Map.of("comparison_id", comparisonId.toString()))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .timeout(Duration.ofMillis(timeoutMs))
                .doOnSuccess(result -> logger.info("Report generated for comparison {}", comparisonId))
                .doOnError(error -> logger.error("Report generation failed: {}", error.getMessage()));

        } catch (Exception e) {
            logger.error("Failed to create report generation request: {}", e.getMessage());
            return Mono.error(new RuntimeException("Report generation request failed", e));
        }
    }

    /**
     * Get AI model information
     * 
     * @param authToken Authentication token
     * @return Model information as JSON
     */
    public Mono<JsonNode> getModelInfo(String authToken) {
        try {
            return webClientBuilder.build()
                .get()
                .uri(aiServiceUrl + "/api/ai/model-info")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .timeout(Duration.ofMillis(timeoutMs))
                .doOnSuccess(result -> logger.info("Model info retrieved successfully"))
                .doOnError(error -> logger.error("Failed to get model info: {}", error.getMessage()));

        } catch (Exception e) {
            logger.error("Failed to get model info: {}", e.getMessage());
            return Mono.error(new RuntimeException("Model info request failed", e));
        }
    }

    /**
     * Check AI service health
     * 
     * @return Health status
     */
    public Mono<JsonNode> checkHealth() {
        try {
            return webClientBuilder.build()
                .get()
                .uri(aiServiceUrl + "/health")
                .retrieve()
                .bodyToMono(JsonNode.class)
                .timeout(Duration.ofMillis(5000))
                .doOnSuccess(result -> logger.debug("AI service health check successful"))
                .doOnError(error -> logger.warn("AI service health check failed: {}", error.getMessage()));

        } catch (Exception e) {
            logger.error("Failed to check AI service health: {}", e.getMessage());
            return Mono.error(new RuntimeException("Health check failed", e));
        }
    }

    /**
     * Detect faces in an image
     * 
     * @param imageId Image ID
     * @param returnLandmarks Whether to return face landmarks
     * @param returnQualityScore Whether to return quality scores
     * @param authToken Authentication token
     * @return Face detection result as JSON
     */
    public Mono<JsonNode> detectFaces(UUID imageId, Boolean returnLandmarks, Boolean returnQualityScore, String authToken) {
        try {
            Map<String, Object> requestBody = Map.of(
                "image_id", imageId.toString(),
                "return_landmarks", returnLandmarks != null ? returnLandmarks : true,
                "return_quality_score", returnQualityScore != null ? returnQualityScore : true
            );

            return webClientBuilder.build()
                .post()
                .uri(aiServiceUrl + "/api/ai/detect-faces")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .timeout(Duration.ofMillis(timeoutMs))
                .doOnSuccess(result -> logger.info("Face detection completed for image {}", imageId))
                .doOnError(error -> logger.error("Face detection failed: {}", error.getMessage()));

        } catch (Exception e) {
            logger.error("Failed to create face detection request: {}", e.getMessage());
            return Mono.error(new RuntimeException("Face detection request failed", e));
        }
    }
}
