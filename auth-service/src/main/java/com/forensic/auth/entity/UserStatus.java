package com.forensic.auth.entity;

/**
 * User account status enumeration
 * 
 * Defines the possible states of a user account
 */
public enum UserStatus {

    /**
     * Active user account - can log in and use the system
     */
    ACTIVE("Active"),

    /**
     * Inactive user account - temporarily disabled
     */
    INACTIVE("Inactive"),

    /**
     * Suspended user account - disabled due to policy violation
     */
    SUSPENDED("Suspended"),

    /**
     * Pending activation - waiting for email verification or admin approval
     */
    PENDING("Pending"),

    /**
     * Expired account - password or account has expired
     */
    EXPIRED("Expired"),

    /**
     * Locked account - temporarily locked due to failed login attempts
     */
    LOCKED("Locked");

    private final String displayName;

    UserStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Check if the user can log in with this status
     */
    public boolean canLogin() {
        return this == ACTIVE;
    }

    /**
     * Check if the user account is disabled
     */
    public boolean isDisabled() {
        return this == INACTIVE || this == SUSPENDED || this == EXPIRED || this == LOCKED;
    }
}
