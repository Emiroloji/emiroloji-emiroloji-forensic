package com.forensic.auth.entity;

/**
 * User roles for role-based access control (RBAC)
 * 
 * Defines the different roles available in the forensic system
 */
public enum Role {

    /**
     * System administrator with full access
     * - User management
     * - System configuration
     * - All case access
     * - Audit log access
     */
    ADMIN("Administrator"),

    /**
     * Senior investigator with broad access
     * - Create and manage cases
     * - Access all cases in their department
     * - Generate reports
     * - Manage junior investigators
     */
    INVESTIGATOR("Investigator"),

    /**
     * Forensic analyst with technical access
     * - Perform face matching analysis
     * - Access assigned cases
     * - Generate technical reports
     * - View analysis results
     */
    ANALYST("Analyst"),

    /**
     * Read-only access for supervisors
     * - View case summaries
     * - Access reports
     * - Monitor system activity
     * - No modification rights
     */
    VIEWER("Viewer"),

    /**
     * External auditor with limited access
     * - View audit logs
     * - Access compliance reports
     * - No case data access
     */
    AUDITOR("Auditor");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Get permissions associated with this role
     */
    public String[] getPermissions() {
        return switch (this) {
            case ADMIN -> new String[] {
                    "USER_MANAGE", "CASE_CREATE", "CASE_READ", "CASE_UPDATE", "CASE_DELETE",
                    "ANALYSIS_PERFORM", "REPORT_GENERATE", "AUDIT_READ", "SYSTEM_CONFIGURE"
            };
            case INVESTIGATOR -> new String[] {
                    "CASE_CREATE", "CASE_READ", "CASE_UPDATE", "ANALYSIS_PERFORM", "REPORT_GENERATE"
            };
            case ANALYST -> new String[] {
                    "CASE_READ", "ANALYSIS_PERFORM", "REPORT_GENERATE"
            };
            case VIEWER -> new String[] {
                    "CASE_READ", "REPORT_READ"
            };
            case AUDITOR -> new String[] {
                    "AUDIT_READ", "REPORT_READ"
            };
        };
    }

    /**
     * Check if this role has a specific permission
     */
    public boolean hasPermission(String permission) {
        String[] permissions = getPermissions();
        for (String perm : permissions) {
            if (perm.equals(permission)) {
                return true;
            }
        }
        return false;
    }
}
