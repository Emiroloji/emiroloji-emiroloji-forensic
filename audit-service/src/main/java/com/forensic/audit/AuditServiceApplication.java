package com.forensic.audit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Audit Service for Forensic Face Matching System
 * 
 * This service provides:
 * - Immutable audit logging
 * - Chain-of-custody tracking
 * - Security event monitoring
 * - Compliance reporting
 * - Forensic evidence integrity
 * - Tamper-proof logging
 */
@SpringBootApplication
@EnableAsync
@EnableScheduling
public class AuditServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuditServiceApplication.class, args);
    }
}
