package com.forensic.audit.entity;

/**
 * Audit Log Status enumeration
 */
public enum AuditLogStatus {
    SUCCESS("Success"),
    FAILURE("Failure"),
    WARNING("Warning"),
    INFO("Info");

    private final String displayName;

    AuditLogStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
