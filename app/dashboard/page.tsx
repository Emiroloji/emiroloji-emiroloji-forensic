"use client"

import { useEffect } from "react"
import { useRouter } from "next/navigation"
import { useAuth } from "@/lib/auth-context"
import { DashboardLayout } from "@/components/dashboard-layout"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { FolderOpen, Scan, FileText, ScrollText } from "lucide-react"

export default function DashboardPage() {
  const router = useRouter()
  const { user, loading } = useAuth()

  useEffect(() => {
    if (!loading && !user) {
      router.push("/login")
    }
  }, [user, loading, router])

  if (loading || !user) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="flex items-center gap-2">
          <div className="h-8 w-8 animate-spin rounded-full border-4 border-primary border-t-transparent" />
          <span className="text-muted-foreground">Loading...</span>
        </div>
      </div>
    )
  }

  const stats = [
    { name: "Active Cases", value: "24", icon: FolderOpen, color: "text-primary" },
    { name: "Analyses Today", value: "12", icon: Scan, color: "text-chart-2" },
    { name: "Files Uploaded", value: "156", icon: FileText, color: "text-chart-3" },
    { name: "Audit Events", value: "89", icon: ScrollText, color: "text-chart-4" },
  ]

  return (
    <DashboardLayout>
      <div className="space-y-6">
        <div>
          <h1 className="text-3xl font-bold">Welcome back, {user.username}</h1>
          <p className="text-muted-foreground mt-1">Here's an overview of your forensic system</p>
        </div>

        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
          {stats.map((stat) => (
            <Card key={stat.name}>
              <CardHeader className="flex flex-row items-center justify-between pb-2">
                <CardTitle className="text-sm font-medium text-muted-foreground">{stat.name}</CardTitle>
                <stat.icon className={`h-5 w-5 ${stat.color}`} />
              </CardHeader>
              <CardContent>
                <div className="text-3xl font-bold">{stat.value}</div>
              </CardContent>
            </Card>
          ))}
        </div>

        <Card>
          <CardHeader>
            <CardTitle>Quick Actions</CardTitle>
            <CardDescription>Common tasks and workflows</CardDescription>
          </CardHeader>
          <CardContent className="grid gap-4 md:grid-cols-2">
            <button
              onClick={() => router.push("/dashboard/cases")}
              className="flex items-center gap-4 p-4 rounded-lg border border-border hover:bg-accent transition-colors text-left"
            >
              <FolderOpen className="h-8 w-8 text-primary" />
              <div>
                <h3 className="font-semibold">Manage Cases</h3>
                <p className="text-sm text-muted-foreground">View and manage forensic cases</p>
              </div>
            </button>

            <button
              onClick={() => router.push("/dashboard/analysis")}
              className="flex items-center gap-4 p-4 rounded-lg border border-border hover:bg-accent transition-colors text-left"
            >
              <Scan className="h-8 w-8 text-chart-2" />
              <div>
                <h3 className="font-semibold">Face Analysis</h3>
                <p className="text-sm text-muted-foreground">Analyze and compare faces</p>
              </div>
            </button>
          </CardContent>
        </Card>
      </div>
    </DashboardLayout>
  )
}
