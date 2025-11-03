import type {
  RegisterRequest,
  LoginResponse,
  Case,
  CreateCaseRequest,
  CaseStatus,
  CasePriority,
  FaceComparison,
  ComparisonDecision,
  ConfidenceLevel,
  FileMetadata,
  AuditLog,
  Page,
  CaseStatistics,
  FaceComparisonStatistics,
} from "./types"

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080/api"

interface ApiResponse<T> {
  data?: T
  error?: string
  message?: string
}

class ApiService {
  private getHeaders(): HeadersInit {
    const headers: HeadersInit = {
      "Content-Type": "application/json",
    }

    const token = localStorage.getItem("accessToken")
    if (token) {
      headers["Authorization"] = `Bearer ${token}`
    }

    return headers
  }

  async request<T>(endpoint: string, options: RequestInit = {}): Promise<ApiResponse<T>> {
    try {
      const response = await fetch(`${API_BASE_URL}${endpoint}`, {
        ...options,
        headers: {
          ...this.getHeaders(),
          ...options.headers,
        },
      })

      if (response.status === 401) {
        const refreshed = await this.refreshToken()
        if (refreshed) {
          return this.request(endpoint, options)
        } else {
          this.clearAuth()
          window.location.href = "/login"
          throw new Error("Session expired")
        }
      }

      const contentType = response.headers.get("content-type")
      const isJson = contentType?.includes("application/json")

      if (!response.ok) {
        const errorData = isJson ? await response.json() : { message: await response.text() }
        return { error: errorData.message || "An error occurred" }
      }

      const data = isJson ? await response.json() : await response.text()
      return { data }
    } catch (error) {
      return { error: error instanceof Error ? error.message : "Network error" }
    }
  }

  async refreshToken(): Promise<boolean> {
    const refreshToken = localStorage.getItem("refreshToken")
    if (!refreshToken) return false

    try {
      const response = await fetch(`${API_BASE_URL}/auth/refresh`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ refreshToken }),
      })

      if (response.ok) {
        const data: LoginResponse = await response.json()
        localStorage.setItem("accessToken", data.accessToken)
        localStorage.setItem("refreshToken", data.refreshToken)
        localStorage.setItem("user", JSON.stringify(data.user))
        return true
      }
      return false
    } catch {
      return false
    }
  }

  clearAuth() {
    localStorage.removeItem("accessToken")
    localStorage.removeItem("refreshToken")
    localStorage.removeItem("user")
  }

  // Auth endpoints
    // Auth endpoints
    async signIn(username: string, password: string): Promise<ApiResponse<LoginResponse>> {
        try {
            const response = await fetch(`${API_BASE_URL}/auth/signin`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                },
                body: JSON.stringify({ username, password }),
            })

            const data = await response.json() // Hata bile olsa JSON'u oku

            if (!response.ok) {
                // Backend'den gelen hatayı göster
                return { error: data.message || "Login failed" }
            }

            // Başarılıysa, token'ları localStorage'a kaydet
            localStorage.setItem("accessToken", data.accessToken)
            localStorage.setItem("refreshToken", data.refreshToken)
            localStorage.setItem("user", JSON.stringify(data.user))
            return { data }
        } catch (error) {
            return { error: error instanceof Error ? error.message : "Network error" }
        }
    }

    async signUp(data: RegisterRequest): Promise<ApiResponse<{ message: string }>> {
        try {
            const response = await fetch(`${API_BASE_URL}/auth/signup`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                },
                body: JSON.stringify(data),
            })

            const responseData = await response.json() // JSON'u oku

            if (!response.ok) {
                return { error: responseData.message || "Signup failed" }
            }

            return { data: responseData }
        } catch (error) {
            return { error: error instanceof Error ? error.message : "Network error" }
        }
    }

    // Case endpoints (Burası devam ediyor...)

  // Case endpoints
  async getCases(page = 0, size = 10) {
    return this.request<Page<Case>>(`/cases?page=${page}&size=${size}`)
  }

  async getCase(id: string) {
    return this.request<Case>(`/cases/${id}`)
  }

  async getCaseByNumber(caseNumber: string) {
    return this.request<Case>(`/cases/number/${encodeURIComponent(caseNumber)}`)
  }

  async createCase(caseData: CreateCaseRequest) {
    return this.request<Case>("/cases", {
      method: "POST",
      body: JSON.stringify(caseData),
    })
  }

  async updateCase(id: string, caseData: Partial<Case>) {
    return this.request<Case>(`/cases/${id}`, {
      method: "PUT",
      body: JSON.stringify(caseData),
    })
  }

  async deleteCase(id: string) {
    return this.request<string>(`/cases/${id}`, {
      method: "DELETE",
    })
  }

  async getCasesByStatus(status: CaseStatus, page = 0, size = 10) {
    return this.request<Page<Case>>(`/cases/status/${status}?page=${page}&size=${size}`)
  }

  async getCasesByPriority(priority: CasePriority, page = 0, size = 10) {
    return this.request<Page<Case>>(`/cases/priority/${priority}?page=${page}&size=${size}`)
  }

  async searchCases(searchTerm: string, page = 0, size = 10) {
    return this.request<Page<Case>>(
      `/cases/search?searchTerm=${encodeURIComponent(searchTerm)}&page=${page}&size=${size}`,
    )
  }

  async getCaseStatistics() {
    return this.request<CaseStatistics>("/cases/statistics")
  }

  // Face Comparison endpoints
  async performFaceComparison(
    caseId: string,
    image1Id: string,
    image2Id: string,
    analyzedBy: string,
    comparisonName?: string,
    threshold?: number,
  ) {
    const params = new URLSearchParams({
      caseId,
      image1Id,
      image2Id,
      analyzedBy,
    })
    if (comparisonName) params.append("comparisonName", comparisonName)
    if (threshold) params.append("threshold", threshold.toString())

    return this.request<FaceComparison>(`/face-comparisons/analyze?${params.toString()}`, {
      method: "POST",
    })
  }

  async getFaceComparison(comparisonId: string) {
    return this.request<FaceComparison>(`/face-comparisons/${comparisonId}`)
  }

  async getFaceComparisonsByCase(caseId: string, page = 0, size = 10) {
    return this.request<Page<FaceComparison>>(`/face-comparisons/case/${caseId}?page=${page}&size=${size}`)
  }

  async getFaceComparisonsByDecision(decision: ComparisonDecision, page = 0, size = 10) {
    return this.request<Page<FaceComparison>>(`/face-comparisons/decision/${decision}?page=${page}&size=${size}`)
  }

  async getFaceComparisonsByConfidence(confidenceLevel: ConfidenceLevel, page = 0, size = 10) {
    return this.request<Page<FaceComparison>>(
      `/face-comparisons/confidence/${confidenceLevel}?page=${page}&size=${size}`,
    )
  }

  async searchFaceComparisons(comparisonName: string, page = 0, size = 10) {
    return this.request<Page<FaceComparison>>(
      `/face-comparisons/search?comparisonName=${encodeURIComponent(comparisonName)}&page=${page}&size=${size}`,
    )
  }

  async getFaceComparisonStatistics() {
    return this.request<FaceComparisonStatistics>("/face-comparisons/statistics")
  }

  async getFaceComparisonStatisticsByCase(caseId: string) {
    return this.request<FaceComparisonStatistics>(`/face-comparisons/statistics/case/${caseId}`)
  }

  // File Storage endpoints
  async uploadFile(file: File, uploadedBy: string, caseId?: string) {
    const formData = new FormData()
    formData.append("file", file)
    formData.append("uploadedBy", uploadedBy)
    if (caseId) formData.append("caseId", caseId)

    const token = localStorage.getItem("accessToken")
    const response = await fetch(`${API_BASE_URL}/storage/upload`, {
      method: "POST",
      headers: {
        Authorization: `Bearer ${token}`,
      },
      body: formData,
    })

    if (!response.ok) {
      const error = await response.json()
      return { error: error.message || "Upload failed" }
    }

    const data = await response.json()
    return { data }
  }

  async downloadFile(fileId: string, userId: string) {
    const token = localStorage.getItem("accessToken")
    return fetch(`${API_BASE_URL}/storage/download/${fileId}?userId=${userId}`, {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    })
  }

  async getFileMetadata(fileId: string) {
    return this.request<FileMetadata>(`/storage/metadata/${fileId}`)
  }

  async deleteFile(fileId: string, userId: string) {
    return this.request<string>(`/storage/${fileId}?userId=${userId}`, {
      method: "DELETE",
    })
  }

  // Audit endpoints
  async getAuditLogs(page = 0, size = 20) {
    return this.request<Page<AuditLog>>(`/audit/logs?page=${page}&size=${size}`)
  }

  async getAuditLogsByUser(userId: string, page = 0, size = 20) {
    return this.request<Page<AuditLog>>(`/audit/user/${userId}?page=${page}&size=${size}`)
  }

  async getAuditLogsByEventType(eventType: string, page = 0, size = 20) {
    return this.request<Page<AuditLog>>(`/audit/event-type/${eventType}?page=${page}&size=${size}`)
  }

  async searchAuditLogs(params: {
    userId?: string
    eventType?: string
    serviceName?: string
    resource?: string
    startDate?: string
    endDate?: string
    page?: number
    size?: number
  }) {
    const queryParams = new URLSearchParams()
    Object.entries(params).forEach(([key, value]) => {
      if (value !== undefined) queryParams.append(key, value.toString())
    })

    return this.request<Page<AuditLog>>(`/audit/search?${queryParams.toString()}`)
  }
}

export const api = new ApiService()
