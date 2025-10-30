package com.forensic.case.repository;

import com.forensic.case.entity.Case;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Case Repository Interface
 * 
 * Provides data access methods for Case entities
 */
@Repository
public interface CaseRepository extends JpaRepository<Case, UUID> {

    /**
     * Find case by case number
     */
    Optional<Case> findByCaseNumber(String caseNumber);

    /**
     * Find cases by investigator ID
     */
    Page<Case> findByInvestigatorId(UUID investigatorId, Pageable pageable);

    /**
     * Find cases by status
     */
    Page<Case> findByStatus(CaseStatus status, Pageable pageable);

    /**
     * Find cases by priority
     */
    Page<Case> findByPriority(CasePriority priority, Pageable pageable);

    /**
     * Find cases by classification
     */
    Page<Case> findByClassification(CaseClassification classification, Pageable pageable);

    /**
     * Find cases by department
     */
    Page<Case> findByDepartment(String department, Pageable pageable);

    /**
     * Find cases by jurisdiction
     */
    Page<Case> findByJurisdiction(String jurisdiction, Pageable pageable);

    /**
     * Find cases by case type
     */
    Page<Case> findByCaseType(String caseType, Pageable pageable);

    /**
     * Find cases by title containing (case-insensitive)
     */
    Page<Case> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    /**
     * Find cases by description containing (case-insensitive)
     */
    Page<Case> findByDescriptionContainingIgnoreCase(String description, Pageable pageable);

    /**
     * Find cases by title or description containing (case-insensitive)
     */
    @Query("SELECT c FROM Case c WHERE LOWER(c.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(c.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Case> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(@Param("searchTerm") String searchTerm, @Param("searchTerm") String searchTerm2, Pageable pageable);

    /**
     * Find cases created after a specific date
     */
    Page<Case> findByCreatedAtAfter(LocalDateTime date, Pageable pageable);

    /**
     * Find cases created before a specific date
     */
    Page<Case> findByCreatedAtBefore(LocalDateTime date, Pageable pageable);

    /**
     * Find cases created between dates
     */
    Page<Case> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    /**
     * Find cases by incident date
     */
    Page<Case> findByIncidentDate(LocalDateTime incidentDate, Pageable pageable);

    /**
     * Find cases by incident date range
     */
    Page<Case> findByIncidentDateBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    /**
     * Find cases by investigator and status
     */
    @Query("SELECT c FROM Case c WHERE c.investigatorId = :investigatorId AND c.status = :status")
    Page<Case> findByInvestigatorIdAndStatus(@Param("investigatorId") UUID investigatorId, @Param("status") CaseStatus status, Pageable pageable);

    /**
     * Find cases by investigator and priority
     */
    @Query("SELECT c FROM Case c WHERE c.investigatorId = :investigatorId AND c.priority = :priority")
    Page<Case> findByInvestigatorIdAndPriority(@Param("investigatorId") UUID investigatorId, @Param("priority") CasePriority priority, Pageable pageable);

    /**
     * Find cases by status and priority
     */
    @Query("SELECT c FROM Case c WHERE c.status = :status AND c.priority = :priority")
    Page<Case> findByStatusAndPriority(@Param("status") CaseStatus status, @Param("priority") CasePriority priority, Pageable pageable);

    /**
     * Find open cases
     */
    @Query("SELECT c FROM Case c WHERE c.status IN ('OPEN', 'IN_PROGRESS')")
    Page<Case> findOpenCases(Pageable pageable);

    /**
     * Find closed cases
     */
    @Query("SELECT c FROM Case c WHERE c.status IN ('CLOSED', 'ARCHIVED')")
    Page<Case> findClosedCases(Pageable pageable);

    /**
     * Find high priority cases
     */
    @Query("SELECT c FROM Case c WHERE c.priority IN ('HIGH', 'CRITICAL')")
    Page<Case> findHighPriorityCases(Pageable pageable);

    /**
     * Find confidential cases
     */
    @Query("SELECT c FROM Case c WHERE c.classification IN ('CONFIDENTIAL', 'SECRET', 'TOP_SECRET')")
    Page<Case> findConfidentialCases(Pageable pageable);

    /**
     * Find cases by multiple criteria
     */
    @Query("SELECT c FROM Case c WHERE " +
           "(:investigatorId IS NULL OR c.investigatorId = :investigatorId) AND " +
           "(:status IS NULL OR c.status = :status) AND " +
           "(:priority IS NULL OR c.priority = :priority) AND " +
           "(:classification IS NULL OR c.classification = :classification) AND " +
           "(:department IS NULL OR c.department = :department)")
    Page<Case> findByMultipleCriteria(@Param("investigatorId") UUID investigatorId,
                                    @Param("status") CaseStatus status,
                                    @Param("priority") CasePriority priority,
                                    @Param("classification") CaseClassification classification,
                                    @Param("department") String department,
                                    Pageable pageable);

    /**
     * Count cases by status
     */
    long countByStatus(CaseStatus status);

    /**
     * Count cases by priority
     */
    long countByPriority(CasePriority priority);

    /**
     * Count cases by classification
     */
    long countByClassification(CaseClassification classification);

    /**
     * Count cases by investigator
     */
    long countByInvestigatorId(UUID investigatorId);

    /**
     * Count cases by department
     */
    long countByDepartment(String department);

    /**
     * Count cases by jurisdiction
     */
    long countByJurisdiction(String jurisdiction);

    /**
     * Count cases by case type
     */
    long countByCaseType(String caseType);

    /**
     * Count cases created after a specific date
     */
    long countByCreatedAtAfter(LocalDateTime date);

    /**
     * Count cases created before a specific date
     */
    long countByCreatedAtBefore(LocalDateTime date);

    /**
     * Count cases created between dates
     */
    long countByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Count cases by incident date range
     */
    long countByIncidentDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Count cases by investigator and status
     */
    @Query("SELECT COUNT(c) FROM Case c WHERE c.investigatorId = :investigatorId AND c.status = :status")
    long countByInvestigatorIdAndStatus(@Param("investigatorId") UUID investigatorId, @Param("status") CaseStatus status);

    /**
     * Count cases by investigator and priority
     */
    @Query("SELECT COUNT(c) FROM Case c WHERE c.investigatorId = :investigatorId AND c.priority = :priority")
    long countByInvestigatorIdAndPriority(@Param("investigatorId") UUID investigatorId, @Param("priority") CasePriority priority);

    /**
     * Count cases by status and priority
     */
    @Query("SELECT COUNT(c) FROM Case c WHERE c.status = :status AND c.priority = :priority")
    long countByStatusAndPriority(@Param("status") CaseStatus status, @Param("priority") CasePriority priority);

    /**
     * Count open cases
     */
    @Query("SELECT COUNT(c) FROM Case c WHERE c.status IN ('OPEN', 'IN_PROGRESS')")
    long countOpenCases();

    /**
     * Count closed cases
     */
    @Query("SELECT COUNT(c) FROM Case c WHERE c.status IN ('CLOSED', 'ARCHIVED')")
    long countClosedCases();

    /**
     * Count high priority cases
     */
    @Query("SELECT COUNT(c) FROM Case c WHERE c.priority IN ('HIGH', 'CRITICAL')")
    long countHighPriorityCases();

    /**
     * Count confidential cases
     */
    @Query("SELECT COUNT(c) FROM Case c WHERE c.classification IN ('CONFIDENTIAL', 'SECRET', 'TOP_SECRET')")
    long countConfidentialCases();

    /**
     * Find cases with most recent activity
     */
    @Query("SELECT c FROM Case c ORDER BY c.updatedAt DESC")
    Page<Case> findCasesWithMostRecentActivity(Pageable pageable);

    /**
     * Find cases with oldest activity
     */
    @Query("SELECT c FROM Case c ORDER BY c.updatedAt ASC")
    Page<Case> findCasesWithOldestActivity(Pageable pageable);

    /**
     * Find cases by title length
     */
    @Query("SELECT c FROM Case c WHERE LENGTH(c.title) > :minLength")
    Page<Case> findByTitleLengthGreaterThan(@Param("minLength") int minLength, Pageable pageable);

    /**
     * Find cases by description length
     */
    @Query("SELECT c FROM Case c WHERE LENGTH(c.description) > :minLength")
    Page<Case> findByDescriptionLengthGreaterThan(@Param("minLength") int minLength, Pageable pageable);

    /**
     * Find cases with no incident date
     */
    @Query("SELECT c FROM Case c WHERE c.incidentDate IS NULL")
    Page<Case> findCasesWithNoIncidentDate(Pageable pageable);

    /**
     * Find cases with no department
     */
    @Query("SELECT c FROM Case c WHERE c.department IS NULL OR c.department = ''")
    Page<Case> findCasesWithNoDepartment(Pageable pageable);

    /**
     * Find cases with no jurisdiction
     */
    @Query("SELECT c FROM Case c WHERE c.jurisdiction IS NULL OR c.jurisdiction = ''")
    Page<Case> findCasesWithNoJurisdiction(Pageable pageable);

    /**
     * Find cases with no case type
     */
    @Query("SELECT c FROM Case c WHERE c.caseType IS NULL OR c.caseType = ''")
    Page<Case> findCasesWithNoCaseType(Pageable pageable);
}
