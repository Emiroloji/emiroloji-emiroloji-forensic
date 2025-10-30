"use client"

import { useEffect, useState } from "react"
import { useRouter } from "next/navigation"
import { useAuth } from "@/lib/auth-context"
import { api } from "@/lib/api"
import type { Case } from "@/lib/types"
import { DashboardLayout } from "@/components/dashboard-layout"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Plus, Search, FolderOpen, Calendar } from "lucide-react"
import Link from "next/link"

export default function CasesPage() {
  const router = useRouter()
  const { user, loading: authLoading } = useAuth()
  const [cases, setCases] = useState<Case[]>([])
  const [loading, setLoading] = useState(true)
  const [searchQuery, setSearchQuery] = useState("")

  useEffect(() => {
    if (!authLoading && !user) {
      router.push("/login")
    }
  }, [user, authLoading, router])

  useEffect(() => {
    if (user) {
      loadCases()
    }
  }, [user])

  const loadCases = async () => {
    setLoading(true)
    const result = await api.getCases(0, 20)
    if (result.data) {
      setCases(result.data.content || [])
    }
    setLoading(false)
  }

  const handleSearch = async () => {
    if (!searchQuery.trim()) {
      loadCases()
      return
    }

    setLoading(true)
    const result = await api.searchCases(searchQuery)
    if (result.data) {
      setCases(result.data.content || [])
    }
    setLoading(false)
  }

  if (authLoading || !user) {
    return null
  }

  const canCreateCase = user.roles?.some((role) => ["INVESTIGATOR", "ADMIN"].includes(role))

  const getStatusColor = (status: string) => {
    switch (status) {
      case "OPEN":
        return "default"
      case "IN_PROGRESS":
        return "secondary"
      case "COMPLETED":
        return "outline"
      case "CLOSED":
        return "outline"
      default:
        return "secondary"
    }
  }

  return (
    <DashboardLayout>
      <div className="space-y-6">
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-3xl font-bold">Cases</h1>
            <p className="text-muted-foreground mt-1">Manage forensic investigation cases</p>
          </div>
          {canCreateCase && (
            <Button onClick={() => router.push("/dashboard/cases/new")}>
              <Plus className="mr-2 h-4 w-4" />
              New Case
            </Button>
          )}
        </div>

        <div className="flex gap-2">
          <div className="relative flex-1">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
            <Input
              placeholder="Search cases by title or description..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              onKeyDown={(e) => e.key === "Enter" && handleSearch()}
              className="pl-9"
            />
          </div>
          <Button onClick={handleSearch}>Search</Button>
        </div>

        {loading ? (
          <div className="flex items-center justify-center py-12">
            <div className="flex items-center gap-2">
              <div className="h-6 w-6 animate-spin rounded-full border-4 border-primary border-t-transparent" />
              <span className="text-muted-foreground">Loading cases...</span>
            </div>
          </div>
        ) : cases.length === 0 ? (
          <Card>
            <CardContent className="flex flex-col items-center justify-center py-12">
              <FolderOpen className="h-12 w-12 text-muted-foreground mb-4" />
              <h3 className="text-lg font-semibold mb-2">No cases found</h3>
              <p className="text-muted-foreground text-center mb-4">
                {searchQuery ? "Try adjusting your search" : "Get started by creating a new case"}
              </p>
              {canCreateCase && !searchQuery && (
                <Button onClick={() => router.push("/dashboard/cases/new")}>
                  <Plus className="mr-2 h-4 w-4" />
                  Create First Case
                </Button>
              )}
            </CardContent>
          </Card>
        ) : (
          <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
            {cases.map((caseItem) => (
              <Link key={caseItem.id} href={`/dashboard/cases/${caseItem.id}`}>
                <Card className="hover:bg-accent transition-colors cursor-pointer h-full">
                  <CardHeader>
                    <div className="flex items-start justify-between gap-2">
                      <CardTitle className="text-lg line-clamp-1">{caseItem.title}</CardTitle>
                      <Badge variant={getStatusColor(caseItem.status)}>{caseItem.status.replace("_", " ")}</Badge>
                    </div>
                    <CardDescription className="line-clamp-2">
                      {caseItem.description || "No description"}
                    </CardDescription>
                  </CardHeader>
                  <CardContent>
                    <div className="space-y-2 text-sm">
                      <div className="flex items-center gap-2 text-muted-foreground">
                        <Calendar className="h-4 w-4" />
                        <span>{new Date(caseItem.createdAt).toLocaleDateString()}</span>
                      </div>
                      <div className="flex items-center gap-2">
                        <Badge variant="outline">{caseItem.priority}</Badge>
                        <span className="text-xs text-muted-foreground">{caseItem.caseNumber}</span>
                      </div>
                    </div>
                  </CardContent>
                </Card>
              </Link>
            ))}
          </div>
        )}
      </div>
    </DashboardLayout>
  )
}
