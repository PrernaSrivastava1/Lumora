import { Play, Info, BarChart2 } from 'lucide-react'

export default function Benchmark() {
  return (
    <div className="p-6 space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">Benchmark Center</h1>
          <p className="text-muted-foreground mt-1">
            Compare speed and accuracy metrics between HNSW, KD-Tree, and Brute Force search algorithms side-by-side.
          </p>
        </div>
        <button
          disabled
          className="inline-flex items-center gap-2 rounded-lg bg-primary px-4 py-2 text-sm font-semibold text-primary-foreground shadow hover:bg-primary/95 transition-all cursor-not-allowed"
        >
          <Play className="h-4 w-4 fill-current" />
          Run Comparative Suite
        </button>
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

      {/* Benchmark Graph Slot */}
      <div className="rounded-xl border border-border bg-card p-6 shadow-sm space-y-4">
        <h2 className="text-lg font-bold tracking-tight flex items-center gap-2">
          <BarChart2 className="h-5 w-5 text-muted-foreground" />
          Algorithmic Latency Comparison (microseconds)
        </h2>
        <div className="border border-dashed border-border rounded-lg h-64 flex flex-col items-center justify-center text-sm text-muted-foreground p-6 gap-2">
          <span>Graphical comparison bar charts will appear here.</span>
          <span className="text-xs text-muted-foreground/60 max-w-sm text-center">
            Watch HNSW execute in sub-millisecond ranges while KD-Tree degrades under high-dimensional data profiles.
          </span>
        </div>
      </div>

      {/* Benchmark summary cards */}
      <div className="grid gap-6 md:grid-cols-3">
        <div className="rounded-xl border border-border bg-card p-5 shadow-sm space-y-3">
          <h3 className="font-bold text-sm text-muted-foreground">Brute Force Exact (Baseline)</h3>
          <div className="space-y-1">
            <div className="text-xs">Complexity: <span className="font-semibold font-mono">O(N • d)</span></div>
            <div className="text-xs">Average Speed: <span className="text-muted-foreground">--</span></div>
            <div className="text-xs">Recall Rate: <span className="text-green-500 font-semibold">100% (Exact)</span></div>
          </div>
        </div>

        <div className="rounded-xl border border-border bg-card p-5 shadow-sm space-y-3">
          <h3 className="font-bold text-sm text-muted-foreground">KD-Tree Spatial</h3>
          <div className="space-y-1">
            <div className="text-xs">Complexity: <span className="font-semibold font-mono">O(log N)</span></div>
            <div className="text-xs">Average Speed: <span className="text-muted-foreground">--</span></div>
            <div className="text-xs">Recall Rate: <span className="text-green-500 font-semibold">100% (Exact)</span></div>
          </div>
        </div>

        <div className="rounded-xl border border-border bg-card p-5 shadow-sm space-y-3">
          <h3 className="font-bold text-sm text-muted-foreground">HNSW Graph (Approximate)</h3>
          <div className="space-y-1">
            <div className="text-xs">Complexity: <span className="font-semibold font-mono">O(log N)</span></div>
            <div className="text-xs">Average Speed: <span className="text-muted-foreground">--</span></div>
            <div className="text-xs">Recall Rate: <span className="text-yellow-500 font-semibold">~95-99% (Approximate)</span></div>
          </div>
        </div>
      </div>
    </div>
  )
}
