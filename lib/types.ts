// Auth types
export interface LoginRequest {
  username: string
  password: string
}

export interface RegisterRequest {
  username: string
  email: string
  password: string
  roles: string[]
  firstName?: string
  lastName?: string
}

export interface LoginResponse {
  accessToken: string
  refreshToken: string
  tokenType: string
  expiresIn: number
  user: UserInfo
  sessionId: string
}

export interface UserInfo {
  id: string
  username: string
  email: string
  firstName: string
  lastName: string
  status: string
  roles: string[]
  twoFactorEnabled: boolean
  lastLogin: string
}

// Case types
export enum CaseStatus {
  OPEN = "OPEN",
  IN_PROGRESS = "IN_PROGRESS",
  COMPLETED = "COMPLETED",
  CLOSED = "CLOSED",
  ARCHIVED = "ARCHIVED",
}

export enum CasePriority {
  LOW = "LOW",
  MEDIUM = "MEDIUM",
  HIGH = "HIGH",
  CRITICAL = "CRITICAL",
}

export enum CaseClassification {
  PUBLIC = "PUBLIC",
  CONFIDENTIAL = "CONFIDENTIAL",
  SECRET = "SECRET",
  TOP_SECRET = "TOP_SECRET",
}

export interface Case {
  id: string
  caseNumber: string
  title: string
  description?: string
  status: CaseStatus
  priority: CasePriority
  classification: CaseClassification
  investigatorId: string
  department?: string
  jurisdiction?: string
  caseType?: string
  incidentDate?: string
  createdAt: string
  updatedAt: string
  closedAt?: string
}

export interface CreateCaseRequest {
  caseNumber: string
  title: string
  description?: string
  priority: CasePriority
  classification: CaseClassification
  investigatorId: string
  department?: string
  jurisdiction?: string
  caseType?: string
  incidentDate?: string
}

// Face Comparison types
export enum ComparisonDecision {
  MATCH = "MATCH",
  NO_MATCH = "NO_MATCH",
  UNCERTAIN = "UNCERTAIN",
}

export enum ConfidenceLevel {
  VERY_HIGH = "VERY_HIGH",
  HIGH = "HIGH",
  MEDIUM = "MEDIUM",
  LOW = "LOW",
  VERY_LOW = "VERY_LOW",
}

export interface FaceComparison {
  id: string
  caseId: string
  comparisonName?: string
  image1Id: string
  image2Id: string
  analysisResult?: string
  matchScore?: number
  decision?: ComparisonDecision
  confidenceLevel?: ConfidenceLevel
  modelVersion?: string
  processingTimeMs?: number
  analyzedBy: string
  analyzedAt: string
  updatedAt: string
}

// File Storage types
export interface FileMetadata {
  id: string
  originalFilename: string
  storedFilename: string
  mimeType: string
  fileSize: number
  caseId?: string
  uploadedBy: string
  uploadedAt: string
  sha256Hash: string
  encrypted: boolean
}

// Audit types
export enum AuditLogStatus {
  SUCCESS = "SUCCESS",
  FAILURE = "FAILURE",
  WARNING = "WARNING",
}

export interface AuditLog {
  id: string
  eventType: string
  serviceName: string
  userId: string
  action: string
  resource: string
  resourceId?: string
  ipAddress: string
  status: AuditLogStatus
  metadata?: Record<string, any>
  timestamp: string
  chainId?: string
  previousHash?: string
  currentHash?: string
}

// Pagination types
export interface Page<T> {
  content: T[]
  pageable: {
    pageNumber: number
    pageSize: number
    sort: {
      sorted: boolean
      unsorted: boolean
      empty: boolean
    }
    offset: number
    paged: boolean
    unpaged: boolean
  }
  totalPages: number
  totalElements: number
  last: boolean
  size: number
  number: number
  sort: {
    sorted: boolean
    unsorted: boolean
    empty: boolean
  }
  numberOfElements: number
  first: boolean
  empty: boolean
}

// Statistics types
export interface CaseStatistics {
  totalCases: number
  openCases: number
  inProgressCases: number
  completedCases: number
  closedCases: number
  archivedCases: number
  highPriorityCases: number
  criticalPriorityCases: number
}

export interface FaceComparisonStatistics {
  totalComparisons: number
  matchCount: number
  noMatchCount: number
  uncertainCount: number
  averageMatchScore: number
  averageProcessingTime: number
}
