import { useState, useEffect } from 'react'
import { Play, Info, BarChart2, Loader2, Award, Zap, Sliders, Database } from 'lucide-react'
import apiClient from '@/services/api'
import { useWorkspaces } from '@/hooks/useWorkspaces'

interface BenchmarkResult {
  algorithm: 'BRUTE_FORCE' | 'KD_TREE' | 'HNSW'
  averageLatency: number
  minimumLatency: number
  maximumLatency: number
  totalExecutions: number
  successRate: number
  accuracy: number
}

export default function Benchmark() {
  const { data: workspaces, isLoading: isWorkspacesLoading } = useWorkspaces()
  const [selectedWorkspaceId, setSelectedWorkspaceId] = useState<number>(0)
  
  const [query, setQuery] = useState('candidate skills')
  const [metric, setMetric] = useState<'COSINE' | 'EUCLIDEAN' | 'MANHATTAN'>('COSINE')
  const [topK] = useState(5)
  const [isRunning, setIsRunning] = useState(false)
  const [errorMsg, setErrorMsg] = useState('')

  const [results, setResults] = useState<BenchmarkResult[]>([])

  // Auto-select workspace
  useEffect(() => {
    if (workspaces && workspaces.length > 0 && selectedWorkspaceId === 0) {
      setSelectedWorkspaceId(workspaces[0].id)
    }
  }, [workspaces])

  const fetchSummary = async () => {
    try {
      const res = await apiClient.get('/benchmark')
      if (res.data && res.data.success) {
        setResults(res.data.data)
      }
    } catch (err) {
      console.error('Failed to fetch benchmark history:', err)
    }
  }

  // Load history summary on mount
  useEffect(() => {
    fetchSummary()
  }, [])

  const runSuite = async () => {
    if (!query.trim() || selectedWorkspaceId === 0) return
    setIsRunning(true)
    setErrorMsg('')
    try {
      const res = await apiClient.get('/benchmark', {
        params: {
          q: query.trim(),
          metric,
          k: topK,
          workspaceId: selectedWorkspaceId
        }
      })
      if (res.data && res.data.success) {
        setResults(res.data.data)
      } else {
        setErrorMsg('Failed to run benchmark suite.')
      }
    } catch (err: any) {
      setErrorMsg(err.response?.data?.message || 'Error occurred while executing comparative suite.')
    } finally {
      setIsRunning(false)
    }
  }

  // Helper to map algorithm names
  const getAlgoName = (type: string) => {
    if (type === 'BRUTE_FORCE') return 'Brute Force'
    if (type === 'KD_TREE') return 'KD-Tree'
    if (type === 'HNSW') return 'HNSW Graph'
    return type
  }

  // SVG chart sizing
  const maxLatency = Math.max(...results.map(r => r.averageLatency), 1)

  return (
    <div className="app-page space-y-7">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <p className="eyebrow mb-2">Evaluation</p>
          <h1 className="text-3xl font-semibold tracking-[-.04em]">
            Benchmark center
          </h1>
          <p className="text-muted-foreground mt-2 max-w-xl">
            Compare retrieval strategies with a clear view of speed, recall, and practical trade-offs.
          </p>
        </div>

        {/* Workspace Selector */}
        <div className="flex items-center gap-2">
          <Database className="h-4 w-4 text-violet-400" />
          <span className="text-sm font-semibold text-muted-foreground">Workspace:</span>
          {isWorkspacesLoading ? (
            <Loader2 className="h-4 w-4 animate-spin text-primary" />
          ) : (
            <select
              value={selectedWorkspaceId}
              onChange={(e) => setSelectedWorkspaceId(Number(e.target.value))}
              className="bg-card border border-border text-foreground rounded-lg px-3 py-1.5 text-sm focus:outline-none focus:ring-1 focus:ring-primary outline-none"
            >
              {workspaces && workspaces.map((ws) => (
                <option key={ws.id} value={ws.id}>
                  {ws.name} ({ws.totalVectors} vectors)
                </option>
              ))}
            </select>
          )}
        </div>
      </div>

      {/* Info Warning */}
      <div className="rounded-lg border border-yellow-500/20 bg-yellow-500/10 p-4 text-xs text-yellow-600 dark:text-yellow-500 flex items-start gap-3">
        <Info className="h-4 w-4 shrink-0 mt-0.5" />
        <div>
          <span className="font-bold">Benchmarking notice:</span> Running benchmarks triggers identical queries
          across HNSW, KD-Tree, and Brute-Force indexes to isolate search speed discrepancies. Ensure your index contains
          ample vectors for meaningful latency measurements.
        </div>
      </div>

      {/* Comparative Form */}
      <div className="rounded-xl border border-border bg-card p-5 shadow-sm space-y-4">
        <h3 className="text-sm font-bold flex items-center gap-1.5 text-foreground">
          <Sliders className="h-4 w-4 text-indigo-400" />
          Benchmark Configuration Parameters
        </h3>
        <div className="grid gap-4 sm:grid-cols-4 items-end">
          <div className="space-y-1.5 sm:col-span-2">
            <span className="text-2xs font-semibold text-muted-foreground">Query Text string</span>
            <input
              type="text"
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              placeholder="Query terms to benchmark..."
              className="w-full px-3 py-1.5 text-sm bg-muted/10 border border-input rounded-lg focus:outline-none focus:ring-1 focus:ring-primary"
            />
          </div>

          <div className="space-y-1.5">
            <span className="text-2xs font-semibold text-muted-foreground">Distance Metric</span>
            <select
              value={metric}
              onChange={(e) => setMetric(e.target.value as any)}
              className="w-full px-2.5 py-1.5 text-sm bg-background border border-input rounded-lg focus:outline-none"
            >
              <option value="COSINE">Cosine Similarity</option>
              <option value="EUCLIDEAN">Euclidean Space</option>
              <option value="MANHATTAN">Manhattan Block</option>
            </select>
          </div>

          <button
            type="button"
            onClick={runSuite}
            disabled={isRunning || !query.trim() || selectedWorkspaceId === 0}
            className="inline-flex items-center justify-center gap-2 rounded-lg bg-primary hover:bg-primary/95 text-primary-foreground font-semibold px-4 py-2 text-sm transition-all disabled:opacity-50"
          >
            {isRunning ? (
              <>
                <Loader2 className="h-4 w-4 animate-spin" />
                Executing Suite...
              </>
            ) : (
              <>
                <Play className="h-4 w-4 fill-current" />
                Run Comparative Suite
              </>
            )}
          </button>
        </div>
        {errorMsg && (
          <div className="text-xs font-semibold text-red-400 bg-red-500/5 border border-red-500/10 p-2.5 rounded-lg">
            {errorMsg}
          </div>
        )}
      </div>

      {results.length > 0 ? (
        <div className="grid gap-6 md:grid-cols-2">
          {/* Latency Chart */}
          <div className="rounded-xl border border-border bg-card p-6 shadow-sm space-y-4">
            <h2 className="text-base font-bold tracking-tight flex items-center gap-2">
              <BarChart2 className="h-4.5 w-4.5 text-violet-400" />
              Latency Comparison (milliseconds)
            </h2>
            <div className="flex flex-col gap-5 pt-2">
              {results.map((r) => {
                const percentage = (r.averageLatency / maxLatency) * 100
                return (
                  <div key={r.algorithm} className="space-y-1.5">
                    <div className="flex justify-between text-xs font-semibold">
                      <span>{getAlgoName(r.algorithm)}</span>
                      <span className="font-mono text-violet-400">{r.averageLatency.toFixed(2)} ms</span>
                    </div>
                    <div className="w-full bg-muted/30 rounded-full h-3 overflow-hidden border border-border/40">
                      <div
                        className="bg-gradient-to-r from-violet-600 to-indigo-600 h-full rounded-full transition-all duration-500"
                        style={{ width: `${Math.max(percentage, 2)}%` }}
                      />
                    </div>
                  </div>
                )
              })}
            </div>
          </div>

          {/* Accuracy Chart */}
          <div className="rounded-xl border border-border bg-card p-6 shadow-sm space-y-4">
            <h2 className="text-base font-bold tracking-tight flex items-center gap-2">
              <Award className="h-4.5 w-4.5 text-emerald-400" />
              Accuracy/Recall Comparison (%)
            </h2>
            <div className="flex flex-col gap-5 pt-2">
              {results.map((r) => {
                const pctVal = r.accuracy * 100
                return (
                  <div key={r.algorithm} className="space-y-1.5">
                    <div className="flex justify-between text-xs font-semibold">
                      <span>{getAlgoName(r.algorithm)}</span>
                      <span className="font-mono text-emerald-400">{pctVal.toFixed(1)}% Accuracy</span>
                    </div>
                    <div className="w-full bg-muted/30 rounded-full h-3 overflow-hidden border border-border/40">
                      <div
                        className="bg-gradient-to-r from-emerald-600 to-teal-500 h-full rounded-full transition-all duration-500"
                        style={{ width: `${Math.max(pctVal, 2)}%` }}
                      />
                    </div>
                  </div>
                )
              })}
            </div>
          </div>
        </div>
      ) : (
        <div className="border border-dashed border-border rounded-xl h-64 flex flex-col items-center justify-center text-sm text-muted-foreground p-6 gap-2 bg-card">
          <Zap className="h-8 w-8 text-indigo-400 animate-pulse" />
          <span>No benchmark runs recorded in this workspace.</span>
          <span className="text-xs text-muted-foreground/60 max-w-sm text-center">
            Specify a search query and click "Run Comparative Suite" to calculate algorithmic latency and accuracy scores.
          </span>
        </div>
      )}

      {/* Benchmark Summary Metadata details */}
      {results.length > 0 && (
        <div className="grid gap-6 md:grid-cols-3">
          {results.map((r) => (
            <div key={r.algorithm} className="rounded-xl border border-border bg-card p-5 shadow-sm space-y-3">
              <h3 className="font-bold text-sm text-foreground flex justify-between items-center pb-2 border-b border-border/40">
                <span>{getAlgoName(r.algorithm)}</span>
                <span className="text-2xs bg-muted border border-border px-1.5 py-0.5 rounded font-mono uppercase">
                  {r.algorithm}
                </span>
              </h3>
              <div className="space-y-2 text-xs text-muted-foreground">
                <div className="flex justify-between">
                  <span>Cumulative Executions:</span>
                  <strong className="text-foreground font-mono">{r.totalExecutions}</strong>
                </div>
                <div className="flex justify-between">
                  <span>Success Rate:</span>
                  <strong className="text-foreground font-mono">{(r.successRate * 100).toFixed(0)}%</strong>
                </div>
                <div className="flex justify-between">
                  <span>Average Time:</span>
                  <strong className="text-foreground font-mono">{r.averageLatency.toFixed(2)} ms</strong>
                </div>
                <div className="flex justify-between">
                  <span>Latency bounds:</span>
                  <strong className="text-foreground font-mono">{r.minimumLatency} - {r.maximumLatency} ms</strong>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
