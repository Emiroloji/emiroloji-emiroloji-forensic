package com.forensic.case;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Case Management Service for Forensic Face Matching System
 * 
 * This service provides:
 * - Case management and tracking
 * - Face comparison analysis
 * - Batch processing capabilities
 * - Integration with AI service
 * - Report generation
 * - Audit trail maintenance
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableAsync
@EnableScheduling
public class CaseServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CaseServiceApplication.class, args);
    }
}
