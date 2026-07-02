import { AlertTriangle, RotateCcw } from 'lucide-react'

export default function ErrorPage({ error, resetErrorBoundary }: { error?: Error; resetErrorBoundary?: () => void }) {
  return (
    <div className="flex min-h-[60vh] flex-col items-center justify-center text-center p-6">
      <div className="rounded-full bg-destructive/15 p-4 mb-4 text-destructive">
        <AlertTriangle className="h-12 w-12" />
      </div>
      <h1 className="text-3xl font-extrabold tracking-tight">Something went wrong</h1>
      <p className="mt-2 text-muted-foreground max-w-md">
        An unexpected error occurred while rendering this page:
      </p>
      {error && (
        <pre className="mt-4 rounded-lg bg-muted p-4 text-xs font-mono text-left max-w-lg overflow-x-auto w-full border border-border">
          {error.message}
        </pre>
      )}
      <div className="mt-6">
        <button
          onClick={() => resetErrorBoundary?.() || window.location.reload()}
          className="inline-flex items-center gap-2 rounded-lg bg-primary px-4 py-2 text-sm font-semibold text-primary-foreground shadow hover:bg-primary/95 transition-all"
        >
          <RotateCcw className="h-4 w-4" />
          Reload Page
        </button>
      </div>
    </div>
  )
}
