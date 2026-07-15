import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import {
  FolderPlus,
  FileUp,
  Search,
  CheckCircle,
  AlertCircle,
  Database,
  Cpu,
  ArrowRight,
  Sparkles,
  BarChart3,
  ListTodo
} from 'lucide-react'
import apiClient from '@/services/api'

interface Workspace {
  id: number
  totalDocuments: number
  totalVectors: number
}

interface BenchmarkResult {
  algorithm: string
  averageLatency: number
  totalExecutions: number
  successRate: number
}

export default function Dashboard() {
  const [stats, setStats] = useState({
    workspaceCount: 0,
    documentCount: 0,
    vectorCount: 0
  })
  const [ollamaOnline, setOllamaOnline] = useState<boolean | null>(null)
  const [benchmarks, setBenchmarks] = useState<BenchmarkResult[]>([])
  
  // Guided stepper demo step state
  const [demoStep, setDemoStep] = useState(1)

  useEffect(() => {
    const fetchDashboardData = async () => {
      try {
        // 1. Fetch workspaces
        const wsRes = await apiClient.get('/workspaces')
        const list: Workspace[] = wsRes.data.data || wsRes.data || []
        
        const docCount = list.reduce((acc, ws) => acc + (ws.totalDocuments || 0), 0)
        const vecCount = list.reduce((acc, ws) => acc + (ws.totalVectors || 0), 0)
        
        setStats({
          workspaceCount: list.length,
          documentCount: docCount,
          vectorCount: vecCount
        })

        // 2. Fetch Ollama health
        const healthRes = await apiClient.get('/health/ollama')
        const healthy = healthRes.data?.data?.healthy ?? false
        setOllamaOnline(healthy)

        // 3. Fetch benchmark results
        const benchmarkRes = await apiClient.get('/benchmark')
        const benchmarkList = benchmarkRes.data?.data || []
        setBenchmarks(benchmarkList)

        // Determine step based on progress
        if (list.length === 0) {
          setDemoStep(1) // Need workspace
        } else if (docCount === 0) {
          setDemoStep(2) // Need upload
        } else {
          setDemoStep(3) // Ready for Search/AI
        }
      } catch (err) {
        console.error('Failed to load dashboard data:', err)
        setOllamaOnline(false)
      }
    }

    fetchDashboardData()
  }, [])

  return (
    <div className="p-6 space-y-6 max-w-6xl mx-auto">
      {/* Welcome Card */}
      <div className="relative overflow-hidden rounded-2xl border border-border bg-card p-6 shadow-lg flex flex-col md:flex-row justify-between items-start md:items-center gap-4 bg-gradient-to-r from-violet-500/5 via-transparent to-cyan-500/5">
        <div>
          <h1 className="text-3xl font-extrabold tracking-tight bg-gradient-to-r from-violet-400 via-fuchsia-400 to-cyan-400 bg-clip-text text-transparent">
            Lumora AI Platform
          </h1>
          <p className="text-sm text-muted-foreground mt-1.5">
            Build, benchmark, and search your custom high-dimensional vector databases locally with explainable AI reasoning.
          </p>
        </div>
        <div className="flex items-center gap-4">
          {ollamaOnline === false ? (
            <div className="flex items-center gap-2 px-3 py-1.5 rounded-lg bg-amber-500/10 text-amber-500 border border-amber-500/20 text-xs font-semibold">
              <AlertCircle className="h-4 w-4" />
              Ollama: Offline
            </div>
          ) : (
            <div className="flex items-center gap-2 px-3 py-1.5 rounded-lg bg-green-500/10 text-green-500 border border-green-500/20 text-xs font-semibold">
              <CheckCircle className="h-4 w-4" />
              Ollama Model: Online
            </div>
          )}
          <div className="flex items-center gap-2 px-3 py-1.5 rounded-lg bg-violet-500/10 text-violet-400 border border-violet-500/20 text-xs font-semibold">
            <CheckCircle className="h-4 w-4" />
            System Status: Active
          </div>
        </div>
      </div>

      {/* Guided Stepper Demo Mode */}
      <div className="rounded-2xl border border-border bg-card p-6 shadow-md space-y-4">
        <div className="flex items-center gap-2 text-violet-400">
          <ListTodo className="h-5 w-5" />
          <h2 className="text-lg font-bold tracking-tight text-foreground">Guided Product Demo Workflow</h2>
        </div>
        <div className="grid gap-4 md:grid-cols-6 text-center text-xs">
          {[
            { step: 1, name: '1. Create Workspace', desc: 'Setup collection' },
            { step: 2, name: '2. Upload Document', desc: 'Extract & vector' },
            { step: 3, name: '3. Watch Processing', desc: 'Pipeline status' },
            { step: 4, name: '4. Vector Search', desc: 'Compare algorithms' },
            { step: 5, name: '5. Ask AI (RAG)', desc: 'Concise answers' },
            { step: 6, name: '6. Check Citations', desc: 'Why this answer?' }
          ].map((item) => (
            <div
              key={item.step}
              className={`rounded-xl border p-3 flex flex-col justify-between transition-all ${
                demoStep === item.step
                  ? 'border-violet-500 bg-violet-500/10 text-foreground ring-1 ring-violet-500'
                  : demoStep > item.step
                  ? 'border-green-500/30 bg-green-500/5 text-muted-foreground'
                  : 'border-border bg-muted/10 text-muted-foreground'
              }`}
            >
              <div className="font-bold text-[11px] mb-1">{item.name}</div>
              <div className="text-[10px] opacity-80">{item.desc}</div>
              {demoStep > item.step && (
                <div className="mt-2 text-emerald-400 font-bold text-[10px]">✓ COMPLETE</div>
              )}
              {demoStep === item.step && (
                <div className="mt-2 text-violet-400 font-bold text-[10px] animate-pulse">ACTIVE STEP</div>
              )}
            </div>
          ))}
        </div>
      </div>

      {/* Stats Cards */}
      <div className="grid gap-6 md:grid-cols-3">
        <div className="rounded-2xl border border-border bg-card p-6 shadow-sm space-y-2">
          <div className="text-xs font-semibold text-muted-foreground uppercase">Workspaces</div>
          <div className="text-3xl font-extrabold text-foreground flex items-center justify-between">
            {stats.workspaceCount}
            <Database className="h-6 w-6 text-violet-400" />
          </div>
        </div>
        <div className="rounded-2xl border border-border bg-card p-6 shadow-sm space-y-2">
          <div className="text-xs font-semibold text-muted-foreground uppercase">Indexed Documents</div>
          <div className="text-3xl font-extrabold text-foreground flex items-center justify-between">
            {stats.documentCount}
            <FileUp className="h-6 w-6 text-fuchsia-400" />
          </div>
        </div>
        <div className="rounded-2xl border border-border bg-card p-6 shadow-sm space-y-2">
          <div className="text-xs font-semibold text-muted-foreground uppercase">Embeddings Generated</div>
          <div className="text-3xl font-extrabold text-foreground flex items-center justify-between">
            {stats.vectorCount}
            <Cpu className="h-6 w-6 text-cyan-400" />
          </div>
        </div>
      </div>

      {/* Action Links & Benchmarks */}
      <div className="grid gap-6 md:grid-cols-2">
        {/* Navigation Actions */}
        <div className="rounded-2xl border border-border bg-card p-6 shadow-md space-y-4">
          <h2 className="text-lg font-bold tracking-tight">Quick Actions</h2>
          <div className="grid gap-4">
            <Link
              to="/workspaces"
              className="group rounded-xl border border-border p-4 flex items-center justify-between hover:border-violet-500/30 hover:bg-violet-500/[0.02] transition-all"
            >
              <div className="flex items-center gap-3">
                <FolderPlus className="h-5 w-5 text-violet-400" />
                <div>
                  <div className="text-sm font-semibold">Workspace Management</div>
                  <div className="text-xs text-muted-foreground">Setup custom vector search segments</div>
                </div>
              </div>
              <ArrowRight className="h-4 w-4 text-muted-foreground group-hover:text-foreground transition-transform group-hover:translate-x-1" />
            </Link>

            <Link
              to="/documents"
              className="group rounded-xl border border-border p-4 flex items-center justify-between hover:border-fuchsia-500/30 hover:bg-fuchsia-500/[0.02] transition-all"
            >
              <div className="flex items-center gap-3">
                <FileUp className="h-5 w-5 text-fuchsia-400" />
                <div>
                  <div className="text-sm font-semibold">Upload & Process Files</div>
                  <div className="text-xs text-muted-foreground">Chunk documents semantically and index</div>
                </div>
              </div>
              <ArrowRight className="h-4 w-4 text-muted-foreground group-hover:text-foreground transition-transform group-hover:translate-x-1" />
            </Link>

            <Link
              to="/search"
              className="group rounded-xl border border-border p-4 flex items-center justify-between hover:border-cyan-500/30 hover:bg-cyan-500/[0.02] transition-all"
            >
              <div className="flex items-center gap-3">
                <Search className="h-5 w-5 text-cyan-400" />
                <div>
                  <div className="text-sm font-semibold">Vector Similarity Engine</div>
                  <div className="text-xs text-muted-foreground">Query using HNSW, KD-Tree or Hybrid modes</div>
                </div>
              </div>
              <ArrowRight className="h-4 w-4 text-muted-foreground group-hover:text-foreground transition-transform group-hover:translate-x-1" />
            </Link>

            <Link
              to="/chat"
              className="group rounded-xl border border-border p-4 flex items-center justify-between hover:border-violet-500/30 hover:bg-violet-500/[0.02] transition-all"
            >
              <div className="flex items-center gap-3">
                <Sparkles className="h-5 w-5 text-violet-400" />
                <div>
                  <div className="text-sm font-semibold">Natural RAG Chat Assistant</div>
                  <div className="text-xs text-muted-foreground">Ask local LLM questions with Explainable debugs</div>
                </div>
              </div>
              <ArrowRight className="h-4 w-4 text-muted-foreground group-hover:text-foreground transition-transform group-hover:translate-x-1" />
            </Link>
          </div>
        </div>

        {/* Algorithm Benchmarks */}
        <div className="rounded-2xl border border-border bg-card p-6 shadow-md space-y-4">
          <div className="flex items-center justify-between">
            <h2 className="text-lg font-bold tracking-tight flex items-center gap-2">
              <BarChart3 className="h-5 w-5 text-cyan-400" />
              Algorithm Latency Performance
            </h2>
            <Link to="/benchmark" className="text-xs text-violet-400 font-semibold hover:underline">
              Run benchmark
            </Link>
          </div>
          {benchmarks.length > 0 ? (
            <div className="space-y-3 pt-2">
              {benchmarks.map((b) => (
                <div key={b.algorithm} className="space-y-1">
                  <div className="flex justify-between text-xs font-semibold">
                    <span>{b.algorithm}</span>
                    <span className="text-muted-foreground">{b.averageLatency.toFixed(1)} ms</span>
                  </div>
                  <div className="w-full bg-muted/40 h-2.5 rounded-full overflow-hidden">
                    <div
                      className="bg-indigo-500 h-full rounded-full transition-all duration-500"
                      style={{ width: `${Math.min(100, (b.averageLatency / 400) * 100)}%` }}
                    />
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <div className="border border-dashed border-border rounded-lg p-10 text-center text-sm text-muted-foreground">
              No benchmark runs recorded. Run a query in the Vector Search page to start capturing latencies.
            </div>
          )}
        </div>
      </div>
    </div>
  )
}
