package com.forensic.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Authentication Service for Forensic Face Matching System
 * 
 * This service provides:
 * - User authentication and authorization
 * - JWT token management
 * - Role-based access control (RBAC)
 * - Two-factor authentication (2FA)
 * - Password policies and security
 * - Session management
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableAsync
public class AuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}
