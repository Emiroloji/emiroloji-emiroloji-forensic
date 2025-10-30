"use client"

import { useEffect, useState } from "react"
import { useAuth } from "@/lib/auth-context"
import { DashboardLayout } from "@/components/dashboard-layout"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Alert, AlertDescription } from "@/components/ui/alert"
import { Badge } from "@/components/ui/badge"
import { AlertCircle, ScrollText, User, Calendar } from "lucide-react"

export default function AuditPage() {
  const { user } = useAuth()
  const [logs, setLogs] = useState<any[]>([])
  const [loading, setLoading] = useState(true)

  const canViewAudit = user?.roles?.some((role) => ["AUDITOR", "ADMIN"].includes(role))

  useEffect(() => {
    if (canViewAudit) {
      loadAuditLogs()
    }
  }, [canViewAudit])

  const loadAuditLogs = async () => {
    setLoading(true)
    // Simulated audit logs
    setTimeout(() => {
      setLogs([
        {
          id: "1",
          action: "CASE_CREATED",
          username: "investigator1",
          timestamp: new Date().toISOString(),
          details: "Created case: Robbery Investigation #2024-001",
        },
        {
          id: "2",
          action: "FILE_UPLOADED",
          username: "analyst2",
          timestamp: new Date(Date.now() - 3600000).toISOString(),
          details: "Uploaded evidence photo to case #2024-001",
        },
        {
          id: "3",
          action: "FACE_ANALYSIS",
          username: "analyst2",
          timestamp: new Date(Date.now() - 7200000).toISOString(),
          details: "Performed face analysis on image IMG_001.jpg",
        },
        {
          id: "4",
          action: "CASE_UPDATED",
          username: "investigator1",
          timestamp: new Date(Date.now() - 10800000).toISOString(),
          details: "Updated case status to IN_PROGRESS",
        },
      ])
      setLoading(false)
    }, 1000)
  }

  if (!canViewAudit) {
    return (
      <DashboardLayout>
        <Alert variant="destructive">
          <AlertCircle className="h-4 w-4" />
          <AlertDescription>You don't have permission to view audit logs.</AlertDescription>
        </Alert>
      </DashboardLayout>
    )
  }

  const getActionColor = (action: string) => {
    switch (action) {
      case "CASE_CREATED":
        return "bg-success/10 text-success"
      case "FILE_UPLOADED":
        return "bg-primary/10 text-primary"
      case "FACE_ANALYSIS":
        return "bg-chart-3/10 text-chart-3"
      case "CASE_UPDATED":
        return "bg-chart-4/10 text-chart-4"
      default:
        return "bg-muted text-muted-foreground"
    }
  }

  return (
    <DashboardLayout>
      <div className="space-y-6">
        <div>
          <h1 className="text-3xl font-bold">Audit Logs</h1>
          <p className="text-muted-foreground mt-1">Immutable record of all system activities</p>
        </div>

        {loading ? (
          <div className="flex items-center justify-center py-12">
            <div className="flex items-center gap-2">
              <div className="h-6 w-6 animate-spin rounded-full border-4 border-primary border-t-transparent" />
              <span className="text-muted-foreground">Loading audit logs...</span>
            </div>
          </div>
        ) : logs.length === 0 ? (
          <Card>
            <CardContent className="flex flex-col items-center justify-center py-12">
              <ScrollText className="h-12 w-12 text-muted-foreground mb-4" />
              <h3 className="text-lg font-semibold mb-2">No audit logs found</h3>
              <p className="text-muted-foreground text-center">System activities will appear here</p>
            </CardContent>
          </Card>
        ) : (
          <Card>
            <CardHeader>
              <CardTitle>Recent Activity</CardTitle>
              <CardDescription>Chronological record of system events</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="space-y-4">
                {logs.map((log) => (
                  <div
                    key={log.id}
                    className="flex gap-4 p-4 rounded-lg border border-border hover:bg-accent transition-colors"
                  >
                    <div className="flex-1 space-y-2">
                      <div className="flex items-center gap-2">
                        <Badge className={getActionColor(log.action)}>{log.action.replace(/_/g, " ")}</Badge>
                        <span className="text-sm text-muted-foreground">{log.details}</span>
                      </div>
                      <div className="flex items-center gap-4 text-sm text-muted-foreground">
                        <div className="flex items-center gap-1">
                          <User className="h-3 w-3" />
                          <span>{log.username}</span>
                        </div>
                        <div className="flex items-center gap-1">
                          <Calendar className="h-3 w-3" />
                          <span>{new Date(log.timestamp).toLocaleString()}</span>
                        </div>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </CardContent>
          </Card>
        )}
      </div>
    </DashboardLayout>
  )
}
