package com.forensic.case.dto.queue;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProgressUpdate {
    private String batchId;
    private int percentage;
    private String message;
    private String status; // STARTED, PROCESSING, COMPLETED, FAILED, CANCELLED
    private LocalDateTime updatedAt;
    private String errorMessage;
    private long estimatedTimeRemaining; // in seconds
}
