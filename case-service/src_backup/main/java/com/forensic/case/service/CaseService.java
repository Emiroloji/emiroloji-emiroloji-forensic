package com.forensic.case.service;

import com.forensic.case.entity.Case;
import com.forensic.case.entity.FaceComparison;
import com.forensic.case.repository.CaseRepository;
import com.forensic.case.repository.FaceComparisonRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Case Service for managing forensic cases
 * 
 * This service provides:
 * - Case CRUD operations
 * - Case status management
 * - Case search and filtering
 * - Case statistics
 * - Integration with face comparison analysis
 */
@Service
@Transactional
public class CaseService {

    private static final Logger logger = LoggerFactory.getLogger(CaseService.class);

    @Autowired
    private CaseRepository caseRepository;

    @Autowired
    private FaceComparisonRepository faceComparisonRepository;

    @Autowired
    private AIServiceClient aiServiceClient;

    /**
     * Create a new case
     * 
     * @param caseEntity Case entity to create
     * @return Created case
     */
    public Case createCase(Case caseEntity) {
        try {
            // Generate case number if not provided
            if (caseEntity.getCaseNumber() == null || caseEntity.getCaseNumber().trim().isEmpty()) {
                caseEntity.setCaseNumber(generateCaseNumber());
            }

            // Set default values
            if (caseEntity.getStatus() == null) {
                caseEntity.setStatus(CaseStatus.OPEN);
            }
            if (caseEntity.getPriority() == null) {
                caseEntity.setPriority(CasePriority.MEDIUM);
            }
            if (caseEntity.getClassification() == null) {
                caseEntity.setClassification(CaseClassification.CONFIDENTIAL);
            }

            Case savedCase = caseRepository.save(caseEntity);
            logger.info("Case created successfully: {} (ID: {})", savedCase.getCaseNumber(), savedCase.getId());
            return savedCase;

        } catch (Exception e) {
            logger.error("Failed to create case: {}", e.getMessage());
            throw new RuntimeException("Case creation failed", e);
        }
    }

    /**
     * Get case by ID
     * 
     * @param caseId Case ID
     * @return Case entity
     */
    @Transactional(readOnly = true)
    public Optional<Case> getCaseById(UUID caseId) {
        try {
            return caseRepository.findById(caseId);
        } catch (Exception e) {
            logger.error("Failed to get case by ID {}: {}", caseId, e.getMessage());
            throw new RuntimeException("Failed to get case", e);
        }
    }

    /**
     * Get case by case number
     * 
     * @param caseNumber Case number
     * @return Case entity
     */
    @Transactional(readOnly = true)
    public Optional<Case> getCaseByNumber(String caseNumber) {
        try {
            return caseRepository.findByCaseNumber(caseNumber);
        } catch (Exception e) {
            logger.error("Failed to get case by number {}: {}", caseNumber, e.getMessage());
            throw new RuntimeException("Failed to get case", e);
        }
    }

    /**
     * Update case
     * 
     * @param caseEntity Case entity to update
     * @return Updated case
     */
    public Case updateCase(Case caseEntity) {
        try {
            Case existingCase = caseRepository.findById(caseEntity.getId())
                .orElseThrow(() -> new RuntimeException("Case not found"));

            // Update fields
            existingCase.setTitle(caseEntity.getTitle());
            existingCase.setDescription(caseEntity.getDescription());
            existingCase.setStatus(caseEntity.getStatus());
            existingCase.setPriority(caseEntity.getPriority());
            existingCase.setClassification(caseEntity.getClassification());
            existingCase.setDepartment(caseEntity.getDepartment());
            existingCase.setJurisdiction(caseEntity.getJurisdiction());
            existingCase.setCaseType(caseEntity.getCaseType());
            existingCase.setIncidentDate(caseEntity.getIncidentDate());

            // Handle status changes
            if (caseEntity.getStatus() == CaseStatus.CLOSED || caseEntity.getStatus() == CaseStatus.ARCHIVED) {
                existingCase.setClosedAt(LocalDateTime.now());
            }

            Case updatedCase = caseRepository.save(existingCase);
            logger.info("Case updated successfully: {} (ID: {})", updatedCase.getCaseNumber(), updatedCase.getId());
            return updatedCase;

        } catch (Exception e) {
            logger.error("Failed to update case: {}", e.getMessage());
            throw new RuntimeException("Case update failed", e);
        }
    }

    /**
     * Delete case (soft delete)
     * 
     * @param caseId Case ID
     */
    public void deleteCase(UUID caseId) {
        try {
            Case caseEntity = caseRepository.findById(caseId)
                .orElseThrow(() -> new RuntimeException("Case not found"));

            // Soft delete by archiving
            caseEntity.setStatus(CaseStatus.ARCHIVED);
            caseEntity.setClosedAt(LocalDateTime.now());
            caseRepository.save(caseEntity);

            logger.info("Case deleted (archived) successfully: {} (ID: {})", caseEntity.getCaseNumber(), caseId);

        } catch (Exception e) {
            logger.error("Failed to delete case {}: {}", caseId, e.getMessage());
            throw new RuntimeException("Case deletion failed", e);
        }
    }

    /**
     * Get all cases with pagination
     * 
     * @param pageable Pagination parameters
     * @return Page of cases
     */
    @Transactional(readOnly = true)
    public Page<Case> getAllCases(Pageable pageable) {
        try {
            return caseRepository.findAll(pageable);
        } catch (Exception e) {
            logger.error("Failed to get all cases: {}", e.getMessage());
            throw new RuntimeException("Failed to get cases", e);
        }
    }

    /**
     * Get cases by investigator
     * 
     * @param investigatorId Investigator ID
     * @param pageable Pagination parameters
     * @return Page of cases
     */
    @Transactional(readOnly = true)
    public Page<Case> getCasesByInvestigator(UUID investigatorId, Pageable pageable) {
        try {
            return caseRepository.findByInvestigatorId(investigatorId, pageable);
        } catch (Exception e) {
            logger.error("Failed to get cases by investigator {}: {}", investigatorId, e.getMessage());
            throw new RuntimeException("Failed to get cases", e);
        }
    }

    /**
     * Get cases by status
     * 
     * @param status Case status
     * @param pageable Pagination parameters
     * @return Page of cases
     */
    @Transactional(readOnly = true)
    public Page<Case> getCasesByStatus(CaseStatus status, Pageable pageable) {
        try {
            return caseRepository.findByStatus(status, pageable);
        } catch (Exception e) {
            logger.error("Failed to get cases by status {}: {}", status, e.getMessage());
            throw new RuntimeException("Failed to get cases", e);
        }
    }

    /**
     * Get cases by priority
     * 
     * @param priority Case priority
     * @param pageable Pagination parameters
     * @return Page of cases
     */
    @Transactional(readOnly = true)
    public Page<Case> getCasesByPriority(CasePriority priority, Pageable pageable) {
        try {
            return caseRepository.findByPriority(priority, pageable);
        } catch (Exception e) {
            logger.error("Failed to get cases by priority {}: {}", priority, e.getMessage());
            throw new RuntimeException("Failed to get cases", e);
        }
    }

    /**
     * Search cases by title or description
     * 
     * @param searchTerm Search term
     * @param pageable Pagination parameters
     * @return Page of cases
     */
    @Transactional(readOnly = true)
    public Page<Case> searchCases(String searchTerm, Pageable pageable) {
        try {
            return caseRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                searchTerm, searchTerm, pageable);
        } catch (Exception e) {
            logger.error("Failed to search cases with term {}: {}", searchTerm, e.getMessage());
            throw new RuntimeException("Case search failed", e);
        }
    }

    /**
     * Get case statistics
     * 
     * @return Case statistics
     */
    @Transactional(readOnly = true)
    public CaseStatistics getCaseStatistics() {
        try {
            long totalCases = caseRepository.count();
            long openCases = caseRepository.countByStatus(CaseStatus.OPEN);
            long inProgressCases = caseRepository.countByStatus(CaseStatus.IN_PROGRESS);
            long completedCases = caseRepository.countByStatus(CaseStatus.COMPLETED);
            long closedCases = caseRepository.countByStatus(CaseStatus.CLOSED);
            long archivedCases = caseRepository.countByStatus(CaseStatus.ARCHIVED);

            long highPriorityCases = caseRepository.countByPriority(CasePriority.HIGH);
            long criticalCases = caseRepository.countByPriority(CasePriority.CRITICAL);

            return new CaseStatistics(
                totalCases, openCases, inProgressCases, completedCases, 
                closedCases, archivedCases, highPriorityCases, criticalCases
            );

        } catch (Exception e) {
            logger.error("Failed to get case statistics: {}", e.getMessage());
            throw new RuntimeException("Failed to get case statistics", e);
        }
    }

    /**
     * Get face comparisons for a case
     * 
     * @param caseId Case ID
     * @param pageable Pagination parameters
     * @return Page of face comparisons
     */
    @Transactional(readOnly = true)
    public Page<FaceComparison> getFaceComparisonsByCase(UUID caseId, Pageable pageable) {
        try {
            return faceComparisonRepository.findByCaseId(caseId, pageable);
        } catch (Exception e) {
            logger.error("Failed to get face comparisons for case {}: {}", caseId, e.getMessage());
            throw new RuntimeException("Failed to get face comparisons", e);
        }
    }

    /**
     * Generate unique case number
     * 
     * @return Generated case number
     */
    private String generateCaseNumber() {
        String prefix = "CASE";
        String timestamp = String.valueOf(System.currentTimeMillis());
        String random = String.valueOf((int) (Math.random() * 1000));
        return prefix + "-" + timestamp.substring(timestamp.length() - 6) + "-" + random;
    }

    /**
     * Case statistics data class
     */
    public static class CaseStatistics {
        private final long totalCases;
        private final long openCases;
        private final long inProgressCases;
        private final long completedCases;
        private final long closedCases;
        private final long archivedCases;
        private final long highPriorityCases;
        private final long criticalCases;

        public CaseStatistics(long totalCases, long openCases, long inProgressCases, 
                            long completedCases, long closedCases, long archivedCases,
                            long highPriorityCases, long criticalCases) {
            this.totalCases = totalCases;
            this.openCases = openCases;
            this.inProgressCases = inProgressCases;
            this.completedCases = completedCases;
            this.closedCases = closedCases;
            this.archivedCases = archivedCases;
            this.highPriorityCases = highPriorityCases;
            this.criticalCases = criticalCases;
        }

        // Getters
        public long getTotalCases() { return totalCases; }
        public long getOpenCases() { return openCases; }
        public long getInProgressCases() { return inProgressCases; }
        public long getCompletedCases() { return completedCases; }
        public long getClosedCases() { return closedCases; }
        public long getArchivedCases() { return archivedCases; }
        public long getHighPriorityCases() { return highPriorityCases; }
        public long getCriticalCases() { return criticalCases; }
    }
}
