package com.forensic.case.service;

import com.forensic.case.dto.queue.ProgressUpdate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ProgressTrackingService {

    private final Map<String, ProgressUpdate> progressMap = new ConcurrentHashMap<>();

    public void updateProgress(String batchId, int percentage, String message, String status) {
        ProgressUpdate progress = ProgressUpdate.builder()
                .batchId(batchId)
                .percentage(percentage)
                .message(message)
                .status(status)
                .updatedAt(LocalDateTime.now())
                .build();

        progressMap.put(batchId, progress);
    }

    public ProgressUpdate getProgress(String batchId) {
        return progressMap.get(batchId);
    }

    public void removeProgress(String batchId) {
        progressMap.remove(batchId);
    }

    public Map<String, ProgressUpdate> getAllProgress() {
        return new ConcurrentHashMap<>(progressMap);
    }

    public void clearCompletedProgress() {
        progressMap.entrySet().removeIf(entry -> 
            "COMPLETED".equals(entry.getValue().getStatus()) || 
            "FAILED".equals(entry.getValue().getStatus()) ||
            "CANCELLED".equals(entry.getValue().getStatus())
        );
    }
}
