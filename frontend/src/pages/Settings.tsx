import { Sliders, Database, Trash2, Cpu, Info } from 'lucide-react'
import { useAuth } from '@/contexts/AuthContext'

export default function Settings() {
  const { isGuest, requireAuth } = useAuth()
  return (
    <div className="app-page space-y-7">
      <div>
        <p className="eyebrow mb-2">System preferences</p>
        <h1 className="text-3xl font-semibold tracking-[-.04em]">Settings</h1>
        <p className="text-muted-foreground mt-1">
          Adjust model weights, local Ollama bindings, database thresholds, and indexing variables.
        </p>
      </div>

      {isGuest && (
        <div className="flex items-start gap-3 rounded-xl border border-primary/20 bg-primary/5 px-4 py-3.5">
          <Info className="h-4 w-4 text-primary shrink-0 mt-0.5" />
          <p className="text-sm text-muted-foreground">
            You're viewing system defaults. <span className="font-medium text-foreground">Sign in</span> to access configuration management and maintenance operations.
          </p>
        </div>
      )}

      <div className="grid gap-6 md:grid-cols-2">
        {/* Model Configurations */}
        <div className="surface p-6 space-y-4">
          <h2 className="text-lg font-bold tracking-tight flex items-center gap-2">
            <Cpu className="h-5 w-5 text-muted-foreground" />
            Ollama Model Configurations
          </h2>
          <div className="space-y-3">
            <div>
              <label className="text-xs font-semibold block mb-1">Embedding Model</label>
              <input
                type="text"
                defaultValue="nomic-embed-text"
                disabled
                className="w-full text-sm border border-input rounded-md px-3 py-1.5 bg-muted/20 cursor-not-allowed"
              />
            </div>
            <div>
              <label className="text-xs font-semibold block mb-1">Generative Model (RAG)</label>
              <input
                type="text"
                defaultValue="llama3.2"
                disabled
                className="w-full text-sm border border-input rounded-md px-3 py-1.5 bg-muted/20 cursor-not-allowed"
              />
            </div>
          </div>
        </div>

        {/* Database Index Configurations */}
        <div className="surface p-6 space-y-4">
          <h2 className="text-lg font-bold tracking-tight flex items-center gap-2">
            <Sliders className="h-5 w-5 text-muted-foreground" />
            HNSW Index Hyperparameters
          </h2>
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="text-xs font-semibold block mb-1">M (Max connections)</label>
              <input
                type="number"
                defaultValue={16}
                disabled
                className="w-full text-sm border border-input rounded-md px-3 py-1.5 bg-muted/20 cursor-not-allowed"
              />
            </div>
            <div>
              <label className="text-xs font-semibold block mb-1">efConstruction (Construction beam search)</label>
              <input
                type="number"
                defaultValue={200}
                disabled
                className="w-full text-sm border border-input rounded-md px-3 py-1.5 bg-muted/20 cursor-not-allowed"
              />
            </div>
          </div>
        </div>

        {/* Dangerous Operations */}
        <div className="surface border-red-500/20 p-6 space-y-4 md:col-span-2">
          <h2 className="text-lg font-bold tracking-tight flex items-center gap-2 text-destructive">
            <Database className="h-5 w-5" />
            System Maintenance
          </h2>
          <div className="flex flex-col sm:flex-row justify-between sm:items-center gap-4 border-t border-border pt-4">
            <div>
              <span className="text-sm font-semibold block">Clear All Indexed Data</span>
              <span className="text-xs text-muted-foreground block">
                Permanently wipes all document stores, chunks, and cached analytics. This cannot be undone.
              </span>
            </div>
            <button
              onClick={() =>
                requireAuth(
                  () => alert('Purge confirmed — implement backend call'),
                  'Sign in to access database maintenance. This operation permanently removes all indexed data.'
                )
              }
              disabled={isGuest}
              className="inline-flex items-center gap-2 rounded-lg bg-destructive px-4 py-2 text-sm font-semibold text-destructive-foreground shadow hover:bg-destructive/95 transition-all shrink-0 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              <Trash2 className="h-4 w-4" />
              Purge Database
            </button>
          </div>
        </div>
      </div>
    </div>
  )
}
