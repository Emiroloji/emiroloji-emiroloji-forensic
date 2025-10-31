package com.forensic.case.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Case entity for managing forensic cases
 */
@Entity
@Table(name = "cases", schema = "cases")
@EntityListeners(AuditingEntityListener.class)
public class Case {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @NotBlank
    @Size(max = 50)
    @Column(name = "case_number", unique = true, nullable = false)
    private String caseNumber;
    
    @NotBlank
    @Size(max = 200)
    @Column(name = "title", nullable = false)
    private String title;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CaseStatus status = CaseStatus.OPEN;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false)
    private CasePriority priority = CasePriority.MEDIUM;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "classification", nullable = false)
    private CaseClassification classification = CaseClassification.CONFIDENTIAL;
    
    @NotNull
    @Column(name = "investigator_id", nullable = false)
    private UUID investigatorId;
    
    @Size(max = 100)
    @Column(name = "department")
    private String department;
    
    @Size(max = 100)
    @Column(name = "jurisdiction")
    private String jurisdiction;
    
    @Size(max = 50)
    @Column(name = "case_type")
    private String caseType;
    
    @Column(name = "incident_date")
    private LocalDateTime incidentDate;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "closed_at")
    private LocalDateTime closedAt;
    
    // Constructors
    public Case() {}
    
    public Case(String caseNumber, String title, String description, UUID investigatorId) {
        this.caseNumber = caseNumber;
        this.title = title;
        this.description = description;
        this.investigatorId = investigatorId;
    }
    
    // Getters and Setters
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public String getCaseNumber() {
        return caseNumber;
    }
    
    public void setCaseNumber(String caseNumber) {
        this.caseNumber = caseNumber;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public CaseStatus getStatus() {
        return status;
    }
    
    public void setStatus(CaseStatus status) {
        this.status = status;
    }
    
    public CasePriority getPriority() {
        return priority;
    }
    
    public void setPriority(CasePriority priority) {
        this.priority = priority;
    }
    
    public CaseClassification getClassification() {
        return classification;
    }
    
    public void setClassification(CaseClassification classification) {
        this.classification = classification;
    }
    
    public UUID getInvestigatorId() {
        return investigatorId;
    }
    
    public void setInvestigatorId(UUID investigatorId) {
        this.investigatorId = investigatorId;
    }
    
    public String getDepartment() {
        return department;
    }
    
    public void setDepartment(String department) {
        this.department = department;
    }
    
    public String getJurisdiction() {
        return jurisdiction;
    }
    
    public void setJurisdiction(String jurisdiction) {
        this.jurisdiction = jurisdiction;
    }
    
    public String getCaseType() {
        return caseType;
    }
    
    public void setCaseType(String caseType) {
        this.caseType = caseType;
    }
    
    public LocalDateTime getIncidentDate() {
        return incidentDate;
    }
    
    public void setIncidentDate(LocalDateTime incidentDate) {
        this.incidentDate = incidentDate;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public LocalDateTime getClosedAt() {
        return closedAt;
    }
    
    public void setClosedAt(LocalDateTime closedAt) {
        this.closedAt = closedAt;
    }
    
    // Business methods
    public boolean isOpen() {
        return status == CaseStatus.OPEN || status == CaseStatus.IN_PROGRESS;
    }
    
    public boolean isClosed() {
        return status == CaseStatus.CLOSED || status == CaseStatus.ARCHIVED;
    }
    
    public void close() {
        this.status = CaseStatus.CLOSED;
        this.closedAt = LocalDateTime.now();
    }
    
    public void archive() {
        this.status = CaseStatus.ARCHIVED;
        this.closedAt = LocalDateTime.now();
    }
    
    public boolean isHighPriority() {
        return priority == CasePriority.HIGH || priority == CasePriority.CRITICAL;
    }
    
    public boolean isConfidential() {
        return classification == CaseClassification.CONFIDENTIAL || 
               classification == CaseClassification.SECRET || 
               classification == CaseClassification.TOP_SECRET;
    }
}

/**
 * Case status enumeration
 */
enum CaseStatus {
    OPEN("Open"),
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed"),
    CLOSED("Closed"),
    ARCHIVED("Archived");
    
    private final String displayName;
    
    CaseStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}

/**
 * Case priority enumeration
 */
enum CasePriority {
    LOW("Low"),
    MEDIUM("Medium"),
    HIGH("High"),
    CRITICAL("Critical");
    
    private final String displayName;
    
    CasePriority(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}

/**
 * Case classification enumeration
 */
enum CaseClassification {
    PUBLIC("Public"),
    CONFIDENTIAL("Confidential"),
    SECRET("Secret"),
    TOP_SECRET("Top Secret");
    
    private final String displayName;
    
    CaseClassification(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
