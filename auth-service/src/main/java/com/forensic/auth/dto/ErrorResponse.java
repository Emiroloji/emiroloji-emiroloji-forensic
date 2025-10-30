package com.forensic.auth.dto;

/**
 * Error Response DTO
 */
public class ErrorResponse {

    private String message;
    private String errorCode;
    private String timestamp;

    // Constructors
    public ErrorResponse() {
        this.timestamp = java.time.LocalDateTime.now().toString();
    }

    public ErrorResponse(String message, String errorCode) {
        this.message = message;
        this.errorCode = errorCode;
        this.timestamp = java.time.LocalDateTime.now().toString();
    }

    // Getters and Setters
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
