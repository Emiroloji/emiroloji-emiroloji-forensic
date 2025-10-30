"use client"

import { useState } from "react"
import { useAuth } from "@/lib/auth-context"
import { DashboardLayout } from "@/components/dashboard-layout"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Alert, AlertDescription } from "@/components/ui/alert"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { Upload, Scan, AlertCircle, CheckCircle2 } from "lucide-react"

export default function AnalysisPage() {
  const { user } = useAuth()
  const [sourceFile, setSourceFile] = useState<File | null>(null)
  const [targetFile, setTargetFile] = useState<File | null>(null)
  const [analyzing, setAnalyzing] = useState(false)
  const [result, setResult] = useState<any>(null)
  const [error, setError] = useState("")

  const canAnalyze = user?.roles?.some((role) => ["ANALYST", "INVESTIGATOR", "ADMIN"].includes(role))

  const handleAnalyze = async () => {
    if (!sourceFile) {
      setError("Please upload a source image")
      return
    }

    setAnalyzing(true)
    setError("")
    setResult(null)

    // Simulated analysis result
    setTimeout(() => {
      setResult({
        similarity: 0.87,
        confidence: 0.92,
        match: true,
        features: {
          faceDetected: true,
          quality: "High",
          landmarks: 68,
        },
      })
      setAnalyzing(false)
    }, 2000)
  }

  const handleCompare = async () => {
    if (!sourceFile || !targetFile) {
      setError("Please upload both source and target images")
      return
    }

    setAnalyzing(true)
    setError("")
    setResult(null)

    // Simulated comparison result
    setTimeout(() => {
      setResult({
        similarity: 0.73,
        confidence: 0.88,
        match: false,
        comparison: {
          euclideanDistance: 0.45,
          cosineSimilarity: 0.73,
          recommendation: "Different individuals",
        },
      })
      setAnalyzing(false)
    }, 2000)
  }

  if (!canAnalyze) {
    return (
      <DashboardLayout>
        <Alert variant="destructive">
          <AlertCircle className="h-4 w-4" />
          <AlertDescription>You don't have permission to perform face analysis.</AlertDescription>
        </Alert>
      </DashboardLayout>
    )
  }

  return (
    <DashboardLayout>
      <div className="space-y-6">
        <div>
          <h1 className="text-3xl font-bold">Face Analysis</h1>
          <p className="text-muted-foreground mt-1">AI-powered face detection and comparison</p>
        </div>

        <Tabs defaultValue="analyze" className="space-y-4">
          <TabsList>
            <TabsTrigger value="analyze">Single Analysis</TabsTrigger>
            <TabsTrigger value="compare">Face Comparison</TabsTrigger>
          </TabsList>

          <TabsContent value="analyze" className="space-y-4">
            <Card>
              <CardHeader>
                <CardTitle>Analyze Face</CardTitle>
                <CardDescription>Upload an image to detect and analyze facial features</CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="border-2 border-dashed border-border rounded-lg p-8 text-center">
                  <Upload className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
                  <input
                    type="file"
                    accept="image/*"
                    onChange={(e) => setSourceFile(e.target.files?.[0] || null)}
                    className="hidden"
                    id="source-upload"
                  />
                  <label htmlFor="source-upload">
                    <Button variant="outline" asChild>
                      <span>Choose Image</span>
                    </Button>
                  </label>
                  {sourceFile && <p className="text-sm text-muted-foreground mt-2">{sourceFile.name}</p>}
                </div>

                <Button onClick={handleAnalyze} disabled={analyzing || !sourceFile} className="w-full">
                  <Scan className="mr-2 h-4 w-4" />
                  {analyzing ? "Analyzing..." : "Analyze Face"}
                </Button>
              </CardContent>
            </Card>
          </TabsContent>

          <TabsContent value="compare" className="space-y-4">
            <div className="grid gap-4 md:grid-cols-2">
              <Card>
                <CardHeader>
                  <CardTitle>Source Image</CardTitle>
                  <CardDescription>Upload the reference face</CardDescription>
                </CardHeader>
                <CardContent>
                  <div className="border-2 border-dashed border-border rounded-lg p-8 text-center">
                    <Upload className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
                    <input
                      type="file"
                      accept="image/*"
                      onChange={(e) => setSourceFile(e.target.files?.[0] || null)}
                      className="hidden"
                      id="source-compare"
                    />
                    <label htmlFor="source-compare">
                      <Button variant="outline" size="sm" asChild>
                        <span>Choose Image</span>
                      </Button>
                    </label>
                    {sourceFile && <p className="text-xs text-muted-foreground mt-2">{sourceFile.name}</p>}
                  </div>
                </CardContent>
              </Card>

              <Card>
                <CardHeader>
                  <CardTitle>Target Image</CardTitle>
                  <CardDescription>Upload the face to compare</CardDescription>
                </CardHeader>
                <CardContent>
                  <div className="border-2 border-dashed border-border rounded-lg p-8 text-center">
                    <Upload className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
                    <input
                      type="file"
                      accept="image/*"
                      onChange={(e) => setTargetFile(e.target.files?.[0] || null)}
                      className="hidden"
                      id="target-compare"
                    />
                    <label htmlFor="target-compare">
                      <Button variant="outline" size="sm" asChild>
                        <span>Choose Image</span>
                      </Button>
                    </label>
                    {targetFile && <p className="text-xs text-muted-foreground mt-2">{targetFile.name}</p>}
                  </div>
                </CardContent>
              </Card>
            </div>

            <Button onClick={handleCompare} disabled={analyzing || !sourceFile || !targetFile} className="w-full">
              <Scan className="mr-2 h-4 w-4" />
              {analyzing ? "Comparing..." : "Compare Faces"}
            </Button>
          </TabsContent>
        </Tabs>

        {error && (
          <Alert variant="destructive">
            <AlertCircle className="h-4 w-4" />
            <AlertDescription>{error}</AlertDescription>
          </Alert>
        )}

        {result && (
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                {result.match ? (
                  <CheckCircle2 className="h-5 w-5 text-success" />
                ) : (
                  <AlertCircle className="h-5 w-5 text-warning" />
                )}
                Analysis Results
              </CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="grid gap-4 md:grid-cols-2">
                <div>
                  <p className="text-sm text-muted-foreground">Similarity Score</p>
                  <p className="text-2xl font-bold">{(result.similarity * 100).toFixed(1)}%</p>
                </div>
                <div>
                  <p className="text-sm text-muted-foreground">Confidence</p>
                  <p className="text-2xl font-bold">{(result.confidence * 100).toFixed(1)}%</p>
                </div>
              </div>

              {result.features && (
                <div className="space-y-2">
                  <h4 className="font-semibold">Features Detected</h4>
                  <div className="grid gap-2 text-sm">
                    <div className="flex justify-between">
                      <span className="text-muted-foreground">Face Detected:</span>
                      <span>{result.features.faceDetected ? "Yes" : "No"}</span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-muted-foreground">Image Quality:</span>
                      <span>{result.features.quality}</span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-muted-foreground">Landmarks:</span>
                      <span>{result.features.landmarks}</span>
                    </div>
                  </div>
                </div>
              )}

              {result.comparison && (
                <div className="space-y-2">
                  <h4 className="font-semibold">Comparison Metrics</h4>
                  <div className="grid gap-2 text-sm">
                    <div className="flex justify-between">
                      <span className="text-muted-foreground">Euclidean Distance:</span>
                      <span>{result.comparison.euclideanDistance}</span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-muted-foreground">Cosine Similarity:</span>
                      <span>{result.comparison.cosineSimilarity}</span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-muted-foreground">Recommendation:</span>
                      <span className="font-medium">{result.comparison.recommendation}</span>
                    </div>
                  </div>
                </div>
              )}
            </CardContent>
          </Card>
        )}
      </div>
    </DashboardLayout>
  )
}
