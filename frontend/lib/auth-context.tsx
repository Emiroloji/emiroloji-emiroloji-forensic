"use client"

import { createContext, useContext, useState, useEffect, type ReactNode } from "react"
import { api } from "./api"
import type { UserInfo } from "./types"

interface AuthContextType {
  user: UserInfo | null
  loading: boolean
  signIn: (username: string, password: string) => Promise<{ success: boolean; error?: string }>
  signOut: () => void
  hasRole: (role: string) => boolean
}

const AuthContext = createContext<AuthContextType | undefined>(undefined)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<UserInfo | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    // Check if user is already logged in
    const token = localStorage.getItem("accessToken")
    const userData = localStorage.getItem("user")

    if (token && userData) {
      try {
        setUser(JSON.parse(userData))
      } catch {
        localStorage.removeItem("user")
      }
    }
    setLoading(false)
  }, [])

  const signIn = async (username: string, password: string) => {
    const response = await api.signIn(username, password)

    if (response.error) {
      return { success: false, error: response.error }
    }

    if (response.data) {
      localStorage.setItem("accessToken", response.data.accessToken)
      localStorage.setItem("refreshToken", response.data.refreshToken)
      localStorage.setItem("user", JSON.stringify(response.data.user))
      setUser(response.data.user)
      return { success: true }
    }

    return { success: false, error: "Unknown error" }
  }

  const signOut = () => {
    api.clearAuth()
    setUser(null)
  }

  const hasRole = (role: string) => {
    return user?.roles?.includes(role) || false
  }

  return <AuthContext.Provider value={{ user, loading, signIn, signOut, hasRole }}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const context = useContext(AuthContext)
  if (context === undefined) {
    throw new Error("useAuth must be used within an AuthProvider")
  }
  return context
}
