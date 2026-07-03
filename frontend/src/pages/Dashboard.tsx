import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import {
  Activity,
  FolderPlus,
  FileUp,
  Search,
  CheckCircle,
  Database,
  Cpu
} from 'lucide-react'
import apiClient from '@/services/api'

interface Workspace {
  id: number
  totalDocuments: number
  totalVectors: number
}

export default function Dashboard() {
  const [stats, setStats] = useState({
    workspaceCount: 0,
    documentCount: 0,
    vectorCount: 0
  })
  const [ollamaOnline, setOllamaOnline] = useState<boolean | null>(null)
  const [isLoading, setIsLoading] = useState(true)

  useEffect(() => {
    const fetchDashboardData = async () => {
      try {
        // 1. Fetch workspaces to sum actual metrics
        const wsRes = await apiClient.get('/workspaces')
        const list: Workspace[] = wsRes.data.data || wsRes.data || []
        
        const docCount = list.reduce((acc, ws) => acc + (ws.totalDocuments || 0), 0)
        const vecCount = list.reduce((acc, ws) => acc + (ws.totalVectors || 0), 0)
        
        setStats({
          workspaceCount: list.length,
          documentCount: docCount,
          vectorCount: vecCount
        })

        // 2. Fetch Ollama health status
        const healthRes = await apiClient.get('/health/ollama')
        const healthy = healthRes.data?.data?.healthy ?? false
        setOllamaOnline(healthy)
      } catch (err) {
        console.error('Failed to load dashboard data:', err)
        setOllamaOnline(false)
      } finally {
        setIsLoading(false)
      }
    }

    fetchDashboardData()
  }, [])

  return (
    <div className="p-6 space-y-6">
      {/* Welcome Card */}
      <div className="rounded-xl border border-border bg-card p-6 shadow-sm flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
          <h1 className="text-2xl font-bold tracking-tight">Welcome to Lumora AI</h1>
          <p className="text-sm text-muted-foreground mt-1">
            Build, benchmark, and search your custom high-dimensional vector databases locally.
          </p>
        </div>
        <div className="flex items-center gap-2 px-3 py-1.5 rounded-lg bg-green-500/10 text-green-500 border border-green-500/20 text-xs font-semibold">
          <CheckCircle className="h-3.5 w-3.5" />
          System Status: Online
        </div>
      </div>

      {/* Quick Action Grid */}
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        <Link
          to="/workspaces"
          className="rounded-xl border border-border bg-card p-4 shadow-sm flex items-center gap-4 hover:border-violet-500/30 hover:bg-violet-500/[0.02] transition-all cursor-pointer"
        >
          <div className="rounded-lg p-2.5 bg-primary/10 text-primary">
            <FolderPlus className="h-5 w-5" />
          </div>
          <div>
            <div className="text-sm font-semibold">New Workspace</div>
            <div className="text-xs text-muted-foreground">Setup knowledge collection</div>
          </div>
        </Link>

        <Link
          to="/documents"
          className="rounded-xl border border-border bg-card p-4 shadow-sm flex items-center gap-4 hover:border-violet-500/30 hover:bg-violet-500/[0.02] transition-all cursor-pointer"
        >
          <div className="rounded-lg p-2.5 bg-primary/10 text-primary">
            <FileUp className="h-5 w-5" />
          </div>
          <div>
            <div className="text-sm font-semibold">Upload Document</div>
            <div className="text-xs text-muted-foreground">Extract & embed text</div>
          </div>
        </Link>

        <Link
          to="/search"
          className="rounded-xl border border-border bg-card p-4 shadow-sm flex items-center gap-4 hover:border-violet-500/30 hover:bg-violet-500/[0.02] transition-all cursor-pointer"
        >
          <div className="rounded-lg p-2.5 bg-primary/10 text-primary">
            <Search className="h-5 w-5" />
          </div>
          <div>
            <div className="text-sm font-semibold">Vector Queries</div>
            <div className="text-xs text-muted-foreground">Test similarity models</div>
          </div>
        </Link>

        <Link
          to="/benchmark"
          className="rounded-xl border border-border bg-card p-4 shadow-sm flex items-center gap-4 hover:border-violet-500/30 hover:bg-violet-500/[0.02] transition-all cursor-pointer"
        >
          <div className="rounded-lg p-2.5 bg-primary/10 text-primary">
            <Cpu className="h-5 w-5" />
          </div>
          <div>
            <div className="text-sm font-semibold">Benchmark Suite</div>
            <div className="text-xs text-muted-foreground">Compare algorithm runs</div>
          </div>
        </Link>
      </div>

      {/* System Status Cards */}
      <div className="grid gap-6 md:grid-cols-3">
        {/* Workspaces Volume */}
        <div className="rounded-xl border border-border bg-card p-5 shadow-sm space-y-4">
          <div className="flex items-center justify-between text-muted-foreground">
            <span className="text-sm font-medium">Workspaces Volume</span>
            <Database className="h-4 w-4" />
          </div>
          <div>
            <div className="text-3xl font-extrabold">{isLoading ? '--' : stats.workspaceCount}</div>
            <div className="text-xs text-muted-foreground mt-1">Total active isolated knowledge domains</div>
          </div>
        </div>

        {/* Index Volume */}
        <div className="rounded-xl border border-border bg-card p-5 shadow-sm space-y-4">
          <div className="flex items-center justify-between text-muted-foreground">
            <span className="text-sm font-medium">Index Volume</span>
            <Activity className="h-4 w-4" />
          </div>
          <div>
            <div className="text-3xl font-extrabold">
              {isLoading ? '--' : `${stats.documentCount} / ${stats.vectorCount}`}
            </div>
            <div className="text-xs text-muted-foreground mt-1">Total documents / indexed vectors</div>
          </div>
        </div>

        {/* Ollama Status */}
        <div className="rounded-xl border border-border bg-card p-5 shadow-sm space-y-4">
          <div className="flex items-center justify-between text-muted-foreground">
            <span className="text-sm font-medium">Ollama Server Connection</span>
            <Cpu className="h-4 w-4" />
          </div>
          <div>
            {ollamaOnline === null ? (
              <div className="text-3xl font-extrabold text-muted-foreground">Checking...</div>
            ) : ollamaOnline ? (
              <div className="text-3xl font-extrabold text-green-500">Online</div>
            ) : (
              <div className="text-3xl font-extrabold text-red-400">Offline</div>
            )}
            <div className="text-xs text-muted-foreground mt-1">
              {ollamaOnline ? 'Embedding model fully operational' : 'Check local ollama serve status'}
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
