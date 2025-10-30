package com.forensic.storage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Storage Service for Forensic Face Matching System
 * 
 * This service provides:
 * - Secure file storage with AES-256 encryption
 * - File metadata extraction and validation
 * - Hash calculation for integrity verification
 * - Virus scanning integration
 * - Access control and audit logging
 * - File versioning and backup
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableAsync
@EnableScheduling
public class StorageServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(StorageServiceApplication.class, args);
    }
}
