package com.forensic.storage.entity;

/**
 * File status enumeration
 */
public enum FileStatus {
    ACTIVE("Active"),
    ARCHIVED("Archived"),
    DELETED("Deleted"),
    QUARANTINED("Quarantined");

    private final String displayName;

    FileStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
