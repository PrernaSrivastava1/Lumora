import { useState, useEffect } from 'react'
import {
  BarChart3,
  Clock,
  CheckCircle2,
  History,
  TrendingUp,
  Database,
  FileText,
  Layers,
  Cpu,
  HelpCircle,
  Trash2
} from 'lucide-react'
import apiClient from '@/services/api'
import { useWorkspaces } from '@/hooks/useWorkspaces'

interface SearchRecord {
  id: number
  timestamp: string
  algorithm: string
  metric: string
  executionTimeMs: number
  topK: number
  totalVectors: number
  resultCount: number
  success: boolean
  errorMessage: string | null
  workspaceId: number
}

interface AnalyticsSummary {
  totalSearches: number
  successfulSearches: number
  successRate: number
  averageLatencyMs: number
  minLatencyMs: number
  maxLatencyMs: number
  searchesByAlgorithm: Record<string, number>
  searchesByMetric: Record<string, number>
}

interface WorkspaceStats {
  documentsCount: number
  chunksCount: number
  embeddingsCount: number
  averageProcessingTimeSec: number
  averageSearchLatencyMs: number
  aiQuestionsAsked: number
}

export default function Analytics() {
  const { data: workspaces } = useWorkspaces()
  const [selectedWorkspaceId, setSelectedWorkspaceId] = useState<number>(0)
  
  // States
  const [wsStats, setWsStats] = useState<WorkspaceStats | null>(null)
  const [summary, setSummary] = useState<AnalyticsSummary | null>(null)
  const [history, setHistory] = useState<SearchRecord[]>([])
  const [isClearing, setIsClearing] = useState(false)

  // Auto-select workspace
  useEffect(() => {
    if (workspaces && workspaces.length > 0 && selectedWorkspaceId === 0) {
      setSelectedWorkspaceId(workspaces[0].id)
    }
  }, [workspaces])

  // Fetch global summary and history
  const fetchGlobalData = async () => {
    try {
      const summaryRes = await apiClient.get('/analytics/summary')
      if (summaryRes.data && summaryRes.data.success) {
        setSummary(summaryRes.data.data)
      }
      
      const historyRes = await apiClient.get('/analytics/history')
      if (historyRes.data && historyRes.data.success) {
        // Sort history descending by timestamp/id
        const sorted = (historyRes.data.data || []).sort((a: any, b: any) => b.id - a.id)
        setHistory(sorted)
      }
    } catch (err) {
      console.error('Failed to load global analytics:', err)
    }
  }

  // Fetch workspace-specific stats
  const fetchWorkspaceStats = async (wsId: number) => {
    if (!wsId) return
    try {
      const statsRes = await apiClient.get(`/workspaces/${wsId}/stats`)
      if (statsRes.data && statsRes.data.success) {
        setWsStats(statsRes.data.data)
      }
    } catch (err) {
      console.error(`Failed to load stats for workspace ${wsId}:`, err)
    }
  }

  useEffect(() => {
    fetchGlobalData()
  }, [])

  useEffect(() => {
    if (selectedWorkspaceId > 0) {
      fetchWorkspaceStats(selectedWorkspaceId)
    }
  }, [selectedWorkspaceId])

  const handleClearHistory = async () => {
    if (!confirm('Are you sure you want to clear all execution log analytics? This cannot be undone.')) return
    setIsClearing(true)
    try {
      await apiClient.delete('/analytics/clear')
      await fetchGlobalData()
      if (selectedWorkspaceId > 0) {
        await fetchWorkspaceStats(selectedWorkspaceId)
      }
    } catch (err) {
      console.error('Failed to clear analytics log:', err)
    } finally {
      setIsClearing(false)
    }
  }

  // Helper for algorithm colors
  const getAlgoColor = (algo: string) => {
    if (algo === 'HNSW') return 'bg-violet-500'
    if (algo === 'KD_TREE') return 'bg-indigo-500'
    if (algo === 'BRUTE_FORCE') return 'bg-cyan-500'
    if (algo === 'HYBRID') return 'bg-fuchsia-500'
    if (algo === 'KEYWORD') return 'bg-rose-500'
    return 'bg-muted'
  }

  return (
    <div className="app-page space-y-7">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <p className="eyebrow mb-2">Observability</p>
          <h1 className="text-3xl font-semibold tracking-[-.04em]">
            Retrieval performance
          </h1>
          <p className="text-muted-foreground mt-2 max-w-xl">
            See how your workspace is indexed, retrieved, and used—without losing the bigger picture.
          </p>
        </div>

        <div className="flex items-center gap-3">
          {/* Workspace Selector */}
          <div className="flex items-center gap-2">
            <Database className="h-4 w-4 text-violet-400" />
            <select
              value={selectedWorkspaceId}
              onChange={(e) => setSelectedWorkspaceId(Number(e.target.value))}
              className="bg-card border border-border text-foreground rounded-lg px-3 py-1.5 text-xs focus:outline-none focus:ring-1 focus:ring-primary outline-none"
            >
              {workspaces && workspaces.map((ws) => (
                <option key={ws.id} value={ws.id}>
                  Stats: {ws.name}
                </option>
              ))}
            </select>
          </div>

          <button
            onClick={handleClearHistory}
            disabled={isClearing || history.length === 0}
            className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-semibold border border-border hover:bg-red-500/5 hover:text-red-400 hover:border-red-500/25 transition-all disabled:opacity-30 disabled:pointer-events-none"
          >
            <Trash2 className="h-3.5 w-3.5" />
            Reset Logs
          </button>
        </div>
      </div>

      {/* Workspace Statistics Cards */}
      {selectedWorkspaceId > 0 && wsStats && (
        <div className="space-y-4">
          <h3 className="text-sm font-bold text-muted-foreground flex items-center gap-1.5">
            <TrendingUp className="h-4 w-4 text-violet-400" />
            Workspace Statistics Summary
          </h3>
          <div className="grid gap-4 sm:grid-cols-3 lg:grid-cols-6">
            <div className="rounded-xl border border-border bg-card p-4 shadow-sm space-y-1">
              <span className="text-[10px] text-muted-foreground font-semibold block uppercase">Documents</span>
              <div className="text-2xl font-bold flex items-center justify-between">
                {wsStats.documentsCount}
                <FileText className="h-5 w-5 text-violet-400 shrink-0" />
              </div>
            </div>

            <div className="rounded-xl border border-border bg-card p-4 shadow-sm space-y-1">
              <span className="text-[10px] text-muted-foreground font-semibold block uppercase">Chunks</span>
              <div className="text-2xl font-bold flex items-center justify-between">
                {wsStats.chunksCount}
                <Layers className="h-5 w-5 text-fuchsia-400 shrink-0" />
              </div>
            </div>

            <div className="rounded-xl border border-border bg-card p-4 shadow-sm space-y-1">
              <span className="text-[10px] text-muted-foreground font-semibold block uppercase">Embeddings</span>
              <div className="text-2xl font-bold flex items-center justify-between">
                {wsStats.embeddingsCount}
                <Cpu className="h-5 w-5 text-cyan-400 shrink-0" />
              </div>
            </div>

            <div className="rounded-xl border border-border bg-card p-4 shadow-sm space-y-1">
              <span className="text-[10px] text-muted-foreground font-semibold block uppercase">Avg Processing</span>
              <div className="text-2xl font-bold flex items-center justify-between">
                {wsStats.averageProcessingTimeSec.toFixed(1)}s
                <Clock className="h-5 w-5 text-emerald-400 shrink-0" />
              </div>
            </div>

            <div className="rounded-xl border border-border bg-card p-4 shadow-sm space-y-1">
              <span className="text-[10px] text-muted-foreground font-semibold block uppercase">Avg Search</span>
              <div className="text-2xl font-bold flex items-center justify-between">
                {wsStats.averageSearchLatencyMs.toFixed(1)}ms
                <Clock className="h-5 w-5 text-indigo-400 shrink-0" />
              </div>
            </div>

            <div className="rounded-xl border border-border bg-card p-4 shadow-sm space-y-1">
              <span className="text-[10px] text-muted-foreground font-semibold block uppercase">AI Questions</span>
              <div className="text-2xl font-bold flex items-center justify-between">
                {wsStats.aiQuestionsAsked}
                <HelpCircle className="h-5 w-5 text-amber-500 shrink-0" />
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Global Analytics Overview */}
      <div className="space-y-4">
        <h3 className="text-sm font-bold text-muted-foreground flex items-center gap-1.5">
          <TrendingUp className="h-4 w-4 text-cyan-400" />
          Global Query Statistics Summary
        </h3>
        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
          <div className="rounded-xl border border-border bg-card p-4 shadow-sm space-y-2">
            <div className="flex items-center justify-between text-muted-foreground">
              <span className="text-xs font-semibold">Total Searches</span>
              <BarChart3 className="h-4 w-4 text-violet-400" />
            </div>
            <div className="text-3xl font-extrabold">{summary ? summary.totalSearches : '--'}</div>
            <div className="text-[10px] text-muted-foreground">Cumulative executions logged</div>
          </div>

          <div className="rounded-xl border border-border bg-card p-4 shadow-sm space-y-2">
            <div className="flex items-center justify-between text-muted-foreground">
              <span className="text-xs font-semibold">Average Latency</span>
              <Clock className="h-4 w-4 text-fuchsia-400" />
            </div>
            <div className="text-3xl font-extrabold">{summary ? summary.averageLatencyMs.toFixed(1) : '--'} ms</div>
            <div className="text-[10px] text-muted-foreground">Mean query execution cost</div>
          </div>

          <div className="rounded-xl border border-border bg-card p-4 shadow-sm space-y-2">
            <div className="flex items-center justify-between text-muted-foreground">
              <span className="text-xs font-semibold">Success Rate</span>
              <CheckCircle2 className="h-4 w-4 text-emerald-400" />
            </div>
            <div className="text-3xl font-extrabold">{summary ? (summary.successRate * 100).toFixed(1) : '--'}%</div>
            <div className="text-[10px] text-muted-foreground">Ratio of finished execution runs</div>
          </div>

          <div className="rounded-xl border border-border bg-card p-4 shadow-sm space-y-2">
            <div className="flex items-center justify-between text-muted-foreground">
              <span className="text-xs font-semibold">Latency Bounds</span>
              <TrendingUp className="h-4 w-4 text-cyan-400" />
            </div>
            <div className="text-3xl font-extrabold">{summary ? `${summary.minLatencyMs}-${summary.maxLatencyMs}` : '--'} ms</div>
            <div className="text-[10px] text-muted-foreground">Min / Max bounds logged</div>
          </div>
        </div>
      </div>

      {/* Latency Charts */}
      {summary && summary.totalSearches > 0 && (
        <div className="grid gap-6 md:grid-cols-2">
          {/* Query Counts by Algorithm */}
          <div className="rounded-xl border border-border bg-card p-6 shadow-sm space-y-4">
            <h3 className="font-bold flex items-center gap-1.5">
              <BarChart3 className="h-4.5 w-4.5 text-violet-400" />
              Query Counts by Search Strategy
            </h3>
            <div className="flex flex-col gap-4">
              {Object.entries(summary.searchesByAlgorithm).map(([algo, count]) => {
                const percentage = summary.totalSearches > 0 ? (count / summary.totalSearches) * 100 : 0
                return (
                  <div key={algo} className="space-y-1">
                    <div className="flex justify-between text-xs font-semibold">
                      <span>{algo}</span>
                      <span>{count} runs</span>
                    </div>
                    <div className="w-full bg-muted/40 h-2.5 rounded-full overflow-hidden border border-border/40">
                      <div
                        className={`${getAlgoColor(algo)} h-full rounded-full transition-all duration-500`}
                        style={{ width: `${Math.max(percentage, 2)}%` }}
                      />
                    </div>
                  </div>
                )
              })}
            </div>
          </div>

          {/* Metric Distribution */}
          <div className="rounded-xl border border-border bg-card p-6 shadow-sm space-y-4">
            <h3 className="font-bold flex items-center gap-1.5">
              <Layers className="h-4.5 w-4.5 text-cyan-400" />
              Query Counts by Distance Metric
            </h3>
            <div className="flex flex-col gap-4">
              {Object.entries(summary.searchesByMetric).map(([metric, count]) => {
                const percentage = summary.totalSearches > 0 ? (count / summary.totalSearches) * 100 : 0
                return (
                  <div key={metric} className="space-y-1">
                    <div className="flex justify-between text-xs font-semibold">
                      <span>{metric}</span>
                      <span>{count} runs</span>
                    </div>
                    <div className="w-full bg-muted/40 h-2.5 rounded-full overflow-hidden border border-border/40">
                      <div
                        className="bg-indigo-500 h-full rounded-full transition-all duration-500"
                        style={{ width: `${Math.max(percentage, 2)}%` }}
                      />
                    </div>
                  </div>
                )
              })}
            </div>
          </div>
        </div>
      )}

      {/* Transaction History log */}
      <div className="rounded-xl border border-border bg-card p-6 shadow-sm space-y-4">
        <h2 className="text-lg font-bold tracking-tight flex items-center gap-2">
          <History className="h-5 w-5 text-muted-foreground" />
          Search Transactions Logs
        </h2>
        {history.length > 0 ? (
          <div className="overflow-x-auto border border-border rounded-lg">
            <table className="w-full text-left border-collapse text-xs">
              <thead>
                <tr className="bg-muted/40 text-muted-foreground font-semibold border-b border-border">
                  <th className="p-3">ID</th>
                  <th className="p-3">Timestamp</th>
                  <th className="p-3">Workspace</th>
                  <th className="p-3">Strategy</th>
                  <th className="p-3">Metric</th>
                  <th className="p-3">Latency</th>
                  <th className="p-3">Hits</th>
                  <th className="p-3">Status</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-border">
                {history.slice(0, 15).map((rec) => (
                  <tr key={rec.id} className="hover:bg-muted/10 transition-colors">
                    <td className="p-3 font-mono font-bold text-muted-foreground">#{rec.id}</td>
                    <td className="p-3">{new Date(rec.timestamp).toLocaleString()}</td>
                    <td className="p-3 font-semibold">Workspace #{rec.workspaceId || 1}</td>
                    <td className="p-3">
                      <span className="font-semibold text-violet-400 bg-violet-500/10 px-1.5 py-0.5 rounded">
                        {rec.algorithm}
                      </span>
                    </td>
                    <td className="p-3 font-mono">{rec.metric}</td>
                    <td className="p-3 font-mono font-bold text-foreground">{rec.executionTimeMs} ms</td>
                    <td className="p-3 font-mono">{rec.resultCount} hits</td>
                    <td className="p-3">
                      <span className={`inline-flex items-center gap-1 rounded-full px-2 py-0.5 text-[10px] font-semibold border ${
                        rec.success
                          ? 'bg-green-500/10 text-green-500 border-green-500/20'
                          : 'bg-red-500/10 text-red-500 border-red-500/20'
                      }`}>
                        {rec.success ? 'Success' : 'Error'}
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : (
          <div className="border border-dashed border-border rounded-lg p-10 text-center text-sm text-muted-foreground">
            No search transactions logged in this session yet. Run queries from the Semantic Search tab.
          </div>
        )}
      </div>
    </div>
  )
}
