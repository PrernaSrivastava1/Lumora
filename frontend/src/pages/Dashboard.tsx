import {
  Activity,
  FolderPlus,
  FileUp,
  Search,
  CheckCircle,
  Database,
  Cpu,
  History,
} from 'lucide-react'

export default function Dashboard() {
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
        <div className="rounded-xl border border-border bg-card p-4 shadow-sm flex items-center gap-4 hover:border-foreground/20 transition-all cursor-not-allowed">
          <div className="rounded-lg p-2.5 bg-primary/10 text-primary">
            <FolderPlus className="h-5 w-5" />
          </div>
          <div>
            <div className="text-sm font-semibold">New Workspace</div>
            <div className="text-xs text-muted-foreground">Setup knowledge collection</div>
          </div>
        </div>

        <div className="rounded-xl border border-border bg-card p-4 shadow-sm flex items-center gap-4 hover:border-foreground/20 transition-all cursor-not-allowed">
          <div className="rounded-lg p-2.5 bg-primary/10 text-primary">
            <FileUp className="h-5 w-5" />
          </div>
          <div>
            <div className="text-sm font-semibold">Upload Document</div>
            <div className="text-xs text-muted-foreground">Extract & embed text</div>
          </div>
        </div>

        <div className="rounded-xl border border-border bg-card p-4 shadow-sm flex items-center gap-4 hover:border-foreground/20 transition-all cursor-not-allowed">
          <div className="rounded-lg p-2.5 bg-primary/10 text-primary">
            <Search className="h-5 w-5" />
          </div>
          <div>
            <div className="text-sm font-semibold">Vector Queries</div>
            <div className="text-xs text-muted-foreground">Test similarity models</div>
          </div>
        </div>

        <div className="rounded-xl border border-border bg-card p-4 shadow-sm flex items-center gap-4 hover:border-foreground/20 transition-all cursor-not-allowed">
          <div className="rounded-lg p-2.5 bg-primary/10 text-primary">
            <Cpu className="h-5 w-5" />
          </div>
          <div>
            <div className="text-sm font-semibold">Benchmark Suite</div>
            <div className="text-xs text-muted-foreground">Compare algorithm runs</div>
          </div>
        </div>
      </div>

      {/* System Status Cards */}
      <div className="grid gap-6 md:grid-cols-3">
        <div className="rounded-xl border border-border bg-card p-5 shadow-sm space-y-4">
          <div className="flex items-center justify-between text-muted-foreground">
            <span className="text-sm font-medium">Index Volume</span>
            <Database className="h-4 w-4" />
          </div>
          <div>
            <div className="text-3xl font-extrabold">20</div>
            <div className="text-xs text-muted-foreground mt-1">Pre-loaded 16D semantic vectors</div>
          </div>
        </div>

        <div className="rounded-xl border border-border bg-card p-5 shadow-sm space-y-4">
          <div className="flex items-center justify-between text-muted-foreground">
            <span className="text-sm font-medium">Recent Queries</span>
            <Activity className="h-4 w-4" />
          </div>
          <div>
            <div className="text-3xl font-extrabold">--</div>
            <div className="text-xs text-muted-foreground mt-1">Total query logs this session</div>
          </div>
        </div>

        <div className="rounded-xl border border-border bg-card p-5 shadow-sm space-y-4">
          <div className="flex items-center justify-between text-muted-foreground">
            <span className="text-sm font-medium">Ollama Server Connection</span>
            <Cpu className="h-4 w-4" />
          </div>
          <div>
            <div className="text-3xl font-extrabold text-muted-foreground">Offline</div>
            <div className="text-xs text-muted-foreground mt-1">Check local ollama serve status</div>
          </div>
        </div>
      </div>

      {/* Recent Activity Placeholder */}
      <div className="rounded-xl border border-border bg-card p-6 shadow-sm space-y-4">
        <div className="flex items-center justify-between">
          <h2 className="text-lg font-bold tracking-tight flex items-center gap-2">
            <History className="h-5 w-5 text-muted-foreground" />
            Recent Activity
          </h2>
        </div>
        <div className="border border-dashed border-border rounded-lg p-8 text-center text-sm text-muted-foreground">
          No recent document uploads or search queries logged in this workspace yet.
        </div>
      </div>
    </div>
  )
}
