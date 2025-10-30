package com.forensic.auth.dto;

/**
 * Message Response DTO
 */
public class MessageResponse {

    private String message;
    private String timestamp;

    // Constructors
    public MessageResponse() {
        this.timestamp = java.time.LocalDateTime.now().toString();
    }

    public MessageResponse(String message) {
        this.message = message;
        this.timestamp = java.time.LocalDateTime.now().toString();
    }

    // Getters and Setters
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
