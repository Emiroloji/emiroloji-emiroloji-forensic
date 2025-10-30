package com.forensic.storage.entity;

/**
 * Virus scan status enumeration
 */
public enum VirusScanStatus {
    PENDING("Pending"),
    CLEAN("Clean"),
    INFECTED("Infected"),
    ERROR("Error");

    private final String displayName;

    VirusScanStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
