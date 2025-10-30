"use client"

import { DashboardLayout } from "@/components/dashboard-layout"
import { Card, CardContent } from "@/components/ui/card"
import { FileText } from "lucide-react"

export default function FilesPage() {
  return (
    <DashboardLayout>
      <div className="space-y-6">
        <div>
          <h1 className="text-3xl font-bold">Files</h1>
          <p className="text-muted-foreground mt-1">Manage case files and evidence</p>
        </div>

        <Card>
          <CardContent className="flex flex-col items-center justify-center py-12">
            <FileText className="h-12 w-12 text-muted-foreground mb-4" />
            <h3 className="text-lg font-semibold mb-2">File Management</h3>
            <p className="text-muted-foreground text-center">Files are managed within individual cases</p>
          </CardContent>
        </Card>
      </div>
    </DashboardLayout>
  )
}
