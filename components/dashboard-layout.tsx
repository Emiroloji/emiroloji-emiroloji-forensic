"use client"

import type { ReactNode } from "react"
import { useRouter, usePathname } from "next/navigation"
import { useAuth } from "@/lib/auth-context"
import { Button } from "@/components/ui/button"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"
import { Shield, FolderOpen, Scan, FileText, ScrollText, User, LogOut, Menu } from "lucide-react"
import Link from "next/link"
import { useState } from "react"

interface DashboardLayoutProps {
  children: ReactNode
}

export function DashboardLayout({ children }: DashboardLayoutProps) {
  const router = useRouter()
  const pathname = usePathname()
  const { user, signOut } = useAuth()
  const [sidebarOpen, setSidebarOpen] = useState(false)

  const handleSignOut = () => {
    signOut()
    router.push("/login")
  }

  const navigation = [
    {
      name: "Cases",
      href: "/dashboard/cases",
      icon: FolderOpen,
      roles: ["VIEWER", "ANALYST", "INVESTIGATOR", "ADMIN"],
    },
    { name: "Face Analysis", href: "/dashboard/analysis", icon: Scan, roles: ["ANALYST", "INVESTIGATOR", "ADMIN"] },
    { name: "Files", href: "/dashboard/files", icon: FileText, roles: ["VIEWER", "ANALYST", "INVESTIGATOR", "ADMIN"] },
    { name: "Audit Logs", href: "/dashboard/audit", icon: ScrollText, roles: ["AUDITOR", "ADMIN"] },
  ]

  const filteredNavigation = navigation.filter((item) => item.roles.some((role) => user?.roles?.includes(role)))

  return (
    <div className="flex h-screen bg-background">
      {/* Sidebar */}
      <aside
        className={`${sidebarOpen ? "translate-x-0" : "-translate-x-full"} md:translate-x-0 fixed md:static inset-y-0 left-0 z-50 w-64 bg-card border-r border-border transition-transform duration-300`}
      >
        <div className="flex flex-col h-full">
          <div className="flex items-center gap-2 p-6 border-b border-border">
            <Shield className="h-8 w-8 text-primary" />
            <div>
              <h1 className="font-bold text-lg">Forensic System</h1>
              <p className="text-xs text-muted-foreground">Case Management</p>
            </div>
          </div>

          <nav className="flex-1 p-4 space-y-2">
            {filteredNavigation.map((item) => {
              const isActive = pathname === item.href
              return (
                <Link
                  key={item.name}
                  href={item.href}
                  className={`flex items-center gap-3 px-3 py-2 rounded-lg transition-colors ${
                    isActive
                      ? "bg-primary text-primary-foreground"
                      : "text-muted-foreground hover:bg-accent hover:text-accent-foreground"
                  }`}
                  onClick={() => setSidebarOpen(false)}
                >
                  <item.icon className="h-5 w-5" />
                  <span className="font-medium">{item.name}</span>
                </Link>
              )
            })}
          </nav>

          <div className="p-4 border-t border-border">
            <DropdownMenu>
              <DropdownMenuTrigger asChild>
                <Button variant="ghost" className="w-full justify-start gap-3">
                  <User className="h-5 w-5" />
                  <div className="flex-1 text-left">
                    <p className="text-sm font-medium">{user?.username}</p>
                    <p className="text-xs text-muted-foreground">{user?.roles?.[0] || "User"}</p>
                  </div>
                </Button>
              </DropdownMenuTrigger>
              <DropdownMenuContent align="end" className="w-56">
                <DropdownMenuLabel>My Account</DropdownMenuLabel>
                <DropdownMenuSeparator />
                <DropdownMenuItem onClick={handleSignOut}>
                  <LogOut className="mr-2 h-4 w-4" />
                  <span>Sign out</span>
                </DropdownMenuItem>
              </DropdownMenuContent>
            </DropdownMenu>
          </div>
        </div>
      </aside>

      {/* Main content */}
      <div className="flex-1 flex flex-col overflow-hidden">
        <header className="flex items-center gap-4 p-4 border-b border-border md:hidden">
          <Button variant="ghost" size="icon" onClick={() => setSidebarOpen(!sidebarOpen)}>
            <Menu className="h-6 w-6" />
          </Button>
          <h1 className="font-bold text-lg">Forensic System</h1>
        </header>

        <main className="flex-1 overflow-auto p-6">{children}</main>
      </div>

      {/* Overlay for mobile */}
      {sidebarOpen && (
        <div
          className="fixed inset-0 bg-background/80 backdrop-blur-sm z-40 md:hidden"
          onClick={() => setSidebarOpen(false)}
        />
      )}
    </div>
  )
}
