import { useState } from 'react'
import { useWorkspaces } from '@/hooks/useWorkspaces'
import { searchService } from '@/services/searchService'
import type { SearchResponse } from '@/types'
import {
  Search as SearchIcon,
  Sliders,
  Play,
  Award,
  Zap,
  Loader2,
  AlertCircle,
  Clock,
  Layers,
  Database,
} from 'lucide-react'

export default function Search() {
  const { data: workspaces, isLoading: isWorkspacesLoading } = useWorkspaces()
  const [selectedWorkspaceId, setSelectedWorkspaceId] = useState<number>(0)

  // Auto-select first workspace when loaded
  if (workspaces && workspaces.length > 0 && selectedWorkspaceId === 0) {
    setSelectedWorkspaceId(workspaces[0].id)
  }

  // Search parameters
  const [query, setQuery] = useState('')
  const [algorithm, setAlgorithm] = useState<'AUTO' | 'BRUTE_FORCE' | 'HNSW' | 'KD_TREE' | 'HYBRID'>('AUTO')
  const [metric, setMetric] = useState<'COSINE' | 'EUCLIDEAN' | 'MANHATTAN'>('COSINE')
  const [topK, setTopK] = useState(5)

  // Request state
  const [isSearching, setIsSearching] = useState(false)
  const [searchResponse, setSearchResponse] = useState<SearchResponse | null>(null)
  const [errorMsg, setErrorMsg] = useState('')

  const handleSearch = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!query.trim() || selectedWorkspaceId === 0) return

    setIsSearching(true)
    setErrorMsg('')
    try {
      const response = await searchService.search({
        query: query.trim(),
        algorithm,
        metric,
        topK,
        workspaceId: selectedWorkspaceId,
      })
      if (response.success) {
        setSearchResponse(response.data)
      } else {
        setErrorMsg(response.message || 'Failed to complete search query')
      }
    } catch (err: any) {
      setErrorMsg(err.response?.data?.message || 'An error occurred during query execution')
      setSearchResponse(null)
    } finally {
      setIsSearching(false)
    }
  }

  return (
    <div className="p-6 space-y-6 max-w-6xl mx-auto">
      <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
        <div>
          <h1 className="text-3xl font-extrabold tracking-tight bg-gradient-to-r from-violet-400 via-fuchsia-400 to-cyan-400 bg-clip-text text-transparent">
            Semantic Search Engine
          </h1>
          <p className="text-muted-foreground mt-1">
            Query your vector index directly using various mathematical models and compare score outputs.
          </p>
        </div>

        {/* Workspace Selector */}
        <div className="flex items-center gap-2">
          <Database className="h-4 w-4 text-violet-400" />
          <span className="text-sm font-semibold text-muted-foreground">Active Workspace:</span>
          {isWorkspacesLoading ? (
            <Loader2 className="h-4 w-4 animate-spin text-primary" />
          ) : (
            <select
              value={selectedWorkspaceId}
              onChange={(e) => {
                setSelectedWorkspaceId(Number(e.target.value))
                setSearchResponse(null)
              }}
              className="bg-card border border-border text-foreground rounded-lg px-3 py-1.5 text-sm focus:outline-none focus:ring-1 focus:ring-primary outline-none"
            >
              {workspaces && workspaces.length > 0 ? (
                workspaces.map((ws) => (
                  <option key={ws.id} value={ws.id}>
                    {ws.name} ({ws.totalVectors} vectors)
                  </option>
                ))
              ) : (
                <option value={0}>No Workspaces Available</option>
              )}
            </select>
          )}
        </div>
      </div>

      {/* Query Formulation Card */}
      <form onSubmit={handleSearch} className="rounded-xl border border-border bg-card p-6 shadow-md space-y-6">
        {/* Input Bar */}
        <div className="relative flex items-center">
          <SearchIcon className="absolute left-3.5 h-5 w-5 text-muted-foreground" />
          <input
            type="text"
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            placeholder="Type search terms (e.g. 'sushi', 'binary tree') or raw dimensions (e.g. '0.9, 0.8, 0.7')"
            className="w-full pl-11 pr-28 py-3 rounded-lg border border-input bg-muted/10 text-sm focus:outline-none focus:ring-1 focus:ring-primary focus:border-primary transition-all placeholder-muted-foreground/60"
          />
          <button
            type="submit"
            disabled={isSearching || !query.trim() || selectedWorkspaceId === 0}
            className="absolute right-2 inline-flex items-center gap-2 rounded-md bg-gradient-to-r from-violet-600 to-indigo-600 px-4 py-2 text-xs font-semibold text-white shadow-lg hover:shadow-indigo-500/20 hover:scale-[1.02] active:scale-[0.98] transition-all disabled:opacity-50 disabled:scale-100 disabled:pointer-events-none"
          >
            {isSearching ? (
              <Loader2 className="h-3.5 w-3.5 animate-spin" />
            ) : (
              <Play className="h-3.5 w-3.5 fill-current" />
            )}
            Search
          </button>
        </div>

        {/* Configuration Selectors */}
        <div className="grid gap-6 md:grid-cols-3 pt-4 border-t border-border">
          {/* Algorithm Type Selector */}
          <div className="space-y-2">
            <span className="text-xs font-semibold text-muted-foreground flex items-center gap-1.5">
              <Sliders className="h-3.5 w-3.5 text-indigo-400" />
              Search Algorithm
            </span>
            <div className="flex flex-wrap gap-2">
              {[
                { name: 'Auto (Smart)', val: 'AUTO' },
                { name: 'Brute Force', val: 'BRUTE_FORCE' },
                { name: 'HNSW', val: 'HNSW' },
                { name: 'KD-Tree', val: 'KD_TREE' },
                { name: 'Hybrid', val: 'HYBRID' },
              ].map((algo) => (
                <button
                  type="button"
                  key={algo.val}
                  onClick={() => setAlgorithm(algo.val as any)}
                  className={`px-3 py-1.5 rounded-lg text-xs font-medium border transition-all ${
                    algorithm === algo.val
                      ? 'border-indigo-500 bg-indigo-500/10 text-indigo-300'
                      : 'border-border bg-muted/20 text-muted-foreground hover:bg-muted/40 hover:text-foreground'
                  }`}
                >
                  {algo.name}
                </button>
              ))}
            </div>
          </div>

          {/* Metric Selector */}
          <div className="space-y-2">
            <span className="text-xs font-semibold text-muted-foreground flex items-center gap-1.5">
              <Sliders className="h-3.5 w-3.5 text-fuchsia-400" />
              Distance Metric
            </span>
            <div className="flex flex-wrap gap-2">
              {[
                { name: 'Cosine', val: 'COSINE' },
                { name: 'Euclidean', val: 'EUCLIDEAN' },
                { name: 'Manhattan', val: 'MANHATTAN' },
              ].map((m) => (
                <button
                  type="button"
                  key={m.val}
                  onClick={() => setMetric(m.val as any)}
                  className={`px-3 py-1.5 rounded-lg text-xs font-medium border transition-all ${
                    metric === m.val
                      ? 'border-fuchsia-500 bg-fuchsia-500/10 text-fuchsia-300'
                      : 'border-border bg-muted/20 text-muted-foreground hover:bg-muted/40 hover:text-foreground'
                  }`}
                >
                  {m.name}
                </button>
              ))}
            </div>
          </div>

          {/* Top K Matches Selector */}
          <div className="space-y-2">
            <span className="text-xs font-semibold text-muted-foreground flex items-center gap-1.5">
              <Sliders className="h-3.5 w-3.5 text-cyan-400" />
              Top-K Limit
            </span>
            <div className="flex items-center gap-4">
              <input
                type="range"
                min="1"
                max="20"
                value={topK}
                onChange={(e) => setTopK(Number(e.target.value))}
                className="w-full h-1.5 rounded-lg bg-border appearance-none cursor-pointer accent-indigo-500"
              />
              <span className="text-sm font-bold text-foreground w-6 text-right">{topK}</span>
            </div>
          </div>
        </div>
      </form>

      {/* Error Output */}
      {errorMsg && (
        <div className="rounded-xl border border-red-500/20 bg-red-500/5 p-4 flex items-start gap-3 text-sm text-red-400 animate-in fade-in duration-200">
          <AlertCircle className="h-5 w-5 shrink-0 mt-0.5" />
          <div>
            <span className="font-semibold">Query Failed:</span> {errorMsg}
          </div>
        </div>
      )}

      {/* Results Section */}
      <div className="rounded-xl border border-border bg-card p-6 shadow-md space-y-6">
        <div className="flex items-center justify-between">
          <h2 className="text-lg font-bold tracking-tight flex items-center gap-2">
            <Award className="h-5 w-5 text-indigo-400" />
            Query Results
          </h2>
          {searchResponse && (
            <div className="flex flex-wrap items-center gap-4 text-xs text-muted-foreground">
              <span className="flex items-center gap-1">
                <Layers className="h-3.5 w-3.5 text-indigo-400" />
                Algorithm: <span className="font-semibold text-foreground">{searchResponse.algorithm}</span>
              </span>
              <span className="flex items-center gap-1">
                <Sliders className="h-3.5 w-3.5 text-fuchsia-400" />
                Dimension: <span className="font-semibold text-foreground">{searchResponse.embeddingDimension}D</span>
              </span>
              <span className="flex items-center gap-1">
                <Clock className="h-3.5 w-3.5 text-cyan-400" />
                Latency: <span className="font-semibold text-foreground">{searchResponse.executionTime} ms</span>
              </span>
              <span className="flex items-center gap-1">
                <Zap className="h-3.5 w-3.5 text-yellow-500" />
                Matches: <span className="font-semibold text-foreground">{searchResponse.resultCount}</span>
              </span>
            </div>
          )}
        </div>

        {searchResponse ? (
          searchResponse.results.length > 0 ? (
            <div className="space-y-4">
              {searchResponse.results.map((hit, index) => (
                <div
                  key={hit.chunkId}
                  className="rounded-lg border border-border bg-muted/5 p-4 space-y-3 hover:border-violet-500/20 hover:bg-violet-500/5 transition-all group animate-in slide-in-from-top-2 duration-200"
                >
                  <div className="flex justify-between items-center text-xs">
                    <span className="font-bold text-violet-400 bg-violet-500/10 px-2 py-1 rounded">
                      Rank #{index + 1}
                    </span>
                    <div className="flex items-center gap-3">
                      <span className="text-muted-foreground">
                        Chunk ID: <span className="text-foreground font-mono">{hit.chunkId}</span>
                      </span>
                      <span className="font-mono font-bold text-emerald-400 bg-emerald-500/10 px-2.5 py-1 rounded">
                        Score: {hit.score.toFixed(6)}
                      </span>
                    </div>
                  </div>

                  <p className="text-sm text-foreground/90 leading-relaxed font-sans pl-1 border-l-2 border-border group-hover:border-violet-500/40 transition-colors">
                    {hit.matchedText || <span className="text-muted-foreground italic">No text content loaded for this vector chunk</span>}
                  </p>

                  <div className="flex justify-between items-center text-2xs text-muted-foreground">
                    <span>{hit.explanation}</span>
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <div className="border border-dashed border-border rounded-lg p-12 text-center text-sm text-muted-foreground">
              No matching vectors found in this workspace. Make sure you have uploaded files and processed embeddings.
            </div>
          )
        ) : (
          <div className="border border-dashed border-border rounded-lg p-12 text-center text-sm text-muted-foreground">
            Enter a search term or a query vector above to calculate similarity values and view matching chunks.
          </div>
        )}
      </div>
    </div>
  )
}
