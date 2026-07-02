import { Search as SearchIcon, Sliders, Play, Award, Zap } from 'lucide-react'

export default function Search() {
  return (
    <div className="p-6 space-y-6">
      <div>
        <h1 className="text-3xl font-bold tracking-tight">Semantic Search</h1>
        <p className="text-muted-foreground mt-1">
          Query your vector index directly using various mathematical models and compare score outputs.
        </p>
      </div>

      {/* Query Formulation Card */}
      <div className="rounded-xl border border-border bg-card p-6 shadow-sm space-y-6">
        {/* Input Bar */}
        <div className="relative flex items-center">
          <SearchIcon className="absolute left-3 h-5 w-5 text-muted-foreground" />
          <input
            type="text"
            placeholder="Type search terms or comma-separated raw dimensions (e.g. 0.9, 0.8, 0.7...)"
            disabled
            className="w-full pl-10 pr-4 py-2.5 rounded-lg border border-input bg-muted/20 text-sm focus:outline-none cursor-not-allowed"
          />
          <button
            disabled
            className="absolute right-2 inline-flex items-center gap-1.5 rounded-md bg-primary px-3 py-1.5 text-xs font-semibold text-primary-foreground shadow hover:bg-primary/95 cursor-not-allowed"
          >
            <Play className="h-3 w-3 fill-current" />
            Query
          </button>
        </div>

        {/* Configuration Selectors */}
        <div className="grid gap-6 md:grid-cols-2 pt-2 border-t border-border">
          {/* Algorithm Type Selector */}
          <div className="space-y-2">
            <span className="text-xs font-semibold text-muted-foreground flex items-center gap-1">
              <Sliders className="h-3.5 w-3.5" />
              Search Algorithm
            </span>
            <div className="flex flex-wrap gap-2">
              {['HNSW', 'KD-Tree', 'Brute Force', 'Hybrid'].map((algo, i) => (
                <button
                  key={algo}
                  disabled
                  className={`px-3 py-1.5 rounded-lg text-xs font-medium border border-border bg-muted/40 text-muted-foreground cursor-not-allowed ${
                    i === 2 ? 'border-primary bg-primary/10 text-foreground' : ''
                  }`}
                >
                  {algo}
                </button>
              ))}
            </div>
          </div>

          {/* Metric Selector */}
          <div className="space-y-2">
            <span className="text-xs font-semibold text-muted-foreground flex items-center gap-1">
              <Sliders className="h-3.5 w-3.5" />
              Distance Metric
            </span>
            <div className="flex flex-wrap gap-2">
              {['Cosine similarity', 'Euclidean distance', 'Manhattan distance'].map((metric, i) => (
                <button
                  key={metric}
                  disabled
                  className={`px-3 py-1.5 rounded-lg text-xs font-medium border border-border bg-muted/40 text-muted-foreground cursor-not-allowed ${
                    i === 0 ? 'border-primary bg-primary/10 text-foreground' : ''
                  }`}
                >
                  {metric}
                </button>
              ))}
            </div>
          </div>
        </div>
      </div>

      {/* Results Section */}
      <div className="rounded-xl border border-border bg-card p-6 shadow-sm space-y-4">
        <div className="flex items-center justify-between">
          <h2 className="text-lg font-bold tracking-tight flex items-center gap-2">
            <Award className="h-5 w-5 text-muted-foreground" />
            Query Results
          </h2>
          <span className="text-xs text-muted-foreground flex items-center gap-1">
            <Zap className="h-3.5 w-3.5 text-yellow-500" />
            Exhaustive Scan
          </span>
        </div>

        <div className="border border-dashed border-border rounded-lg p-10 text-center text-sm text-muted-foreground">
          Enter a query vector to calculate similarity values and view matching indexes.
        </div>
      </div>
    </div>
  )
}
