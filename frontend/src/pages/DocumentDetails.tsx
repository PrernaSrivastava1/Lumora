import { useParams, Link } from 'react-router-dom'
import { useDocumentById } from '@/hooks/useDocuments'
import {
  FileText,
  ArrowLeft,
  Calendar,
  Layers,
  Database,
  CheckCircle2,
  HardDrive,
  Info,
  AlertCircle,
  Clock,
} from 'lucide-react'

export default function DocumentDetails() {
  const { id } = useParams<{ id: string }>()
  const documentId = Number(id)

  const { data: doc, isLoading, isError, error } = useDocumentById(documentId)

  const formatBytes = (bytes: number) => {
    if (bytes === 0) return '0 Bytes'
    const k = 1024
    const sizes = ['Bytes', 'KB', 'MB']
    const i = Math.floor(Math.log(bytes) / Math.log(k))
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
  }

  return (
    <div className="p-6 space-y-6">
      {/* Header with Back button */}
      <div className="flex items-center gap-4">
        <Link
          to="/documents"
          className="p-1.5 rounded-lg border border-border bg-card text-muted-foreground hover:text-foreground hover:bg-secondary transition-colors"
          title="Back to Documents"
        >
          <ArrowLeft className="h-4 w-4" />
        </Link>
        <div>
          <h1 className="text-2xl font-bold tracking-tight">Document Details</h1>
          <p className="text-xs text-muted-foreground mt-0.5">
            Metadata summary and processing telemetry logs.
          </p>
        </div>
      </div>

      {isLoading && (
        <div className="rounded-xl border border-border bg-card p-6 space-y-4 animate-pulse">
          <div className="h-6 w-1/3 bg-muted rounded" />
          <div className="grid gap-4 md:grid-cols-2">
            <div className="h-10 bg-muted rounded" />
            <div className="h-10 bg-muted rounded" />
          </div>
        </div>
      )}

      {isError && (
        <div className="rounded-lg border border-destructive/20 bg-destructive/10 p-4 text-sm text-destructive flex items-start gap-3">
          <AlertCircle className="h-5 w-5 shrink-0" />
          <div>
            <span className="font-bold">Error loading document details:</span>{' '}
            {error instanceof Error ? error.message : 'Unknown database error'}
          </div>
        </div>
      )}

      {!isLoading && !isError && doc && (
        <div className="grid gap-6 md:grid-cols-3">
          {/* Main Info Card */}
          <div className="rounded-xl border border-border bg-card p-6 shadow-sm space-y-6 md:col-span-2">
            <div className="flex items-start gap-4">
              <div className="rounded-xl p-3.5 bg-primary/10 text-primary">
                <FileText className="h-8 w-8" />
              </div>
              <div className="min-w-0 flex-1">
                <h2 className="text-xl font-bold truncate">{doc.title}</h2>
                <div className="flex items-center gap-2 mt-1.5">
                  <span className="inline-flex items-center gap-1 rounded-full bg-green-500/10 px-2 py-0.5 text-xs font-semibold text-green-500 border border-green-500/20">
                    <CheckCircle2 className="h-3 w-3" />
                    Ready
                  </span>
                  <span className="text-xs text-muted-foreground">ID: {doc.id}</span>
                </div>
              </div>
            </div>

            {/* Metadata Grid */}
            <div className="grid gap-4 sm:grid-cols-2 pt-4 border-t border-border">
              <div className="flex items-center gap-3">
                <HardDrive className="h-4 w-4 text-muted-foreground shrink-0" />
                <div>
                  <span className="text-[10px] text-muted-foreground block">File Size</span>
                  <span className="text-sm font-semibold">{formatBytes(doc.size)}</span>
                </div>
              </div>

              <div className="flex items-center gap-3">
                <Layers className="h-4 w-4 text-muted-foreground shrink-0" />
                <div>
                  <span className="text-[10px] text-muted-foreground block">Format Extension</span>
                  <span className="text-sm font-semibold">{doc.fileType}</span>
                </div>
              </div>

              <div className="flex items-center gap-3">
                <Calendar className="h-4 w-4 text-muted-foreground shrink-0" />
                <div>
                  <span className="text-[10px] text-muted-foreground block">Upload Time</span>
                  <span className="text-sm font-semibold">{new Date(doc.uploadTime).toLocaleString()}</span>
                </div>
              </div>

              <div className="flex items-center gap-3">
                <Clock className="h-4 w-4 text-muted-foreground shrink-0" />
                <div>
                  <span className="text-[10px] text-muted-foreground block">Target Workspace ID</span>
                  <span className="text-sm font-semibold">Workspace #{doc.workspaceId}</span>
                </div>
              </div>
            </div>

            {/* Local Path Note */}
            <div className="rounded-lg bg-muted/40 p-4 border border-border space-y-2">
              <span className="text-xs font-bold block">Stored Filename (Disk)</span>
              <code className="text-xs font-mono block break-all bg-background border border-border p-2 rounded">
                {doc.originalFileName}
              </code>
            </div>
          </div>

          {/* Slices & Embeddings Telemetry Side Panel */}
          <div className="rounded-xl border border-border bg-card p-6 shadow-sm space-y-6 md:col-span-1 h-fit">
            <h3 className="font-bold flex items-center gap-2">
              <Database className="h-5 w-5 text-muted-foreground" />
              Segmentation
            </h3>

            <div className="space-y-4">
              <div className="flex items-center justify-between text-sm">
                <span className="text-muted-foreground">Total Chunks</span>
                <span className="font-bold font-mono">{doc.totalChunks}</span>
              </div>
              <div className="flex items-center justify-between text-sm">
                <span className="text-muted-foreground">Vector Dimension</span>
                <span className="font-semibold text-muted-foreground">Pending</span>
              </div>
            </div>

            <div className="rounded-lg border border-primary/20 bg-primary/5 p-4 text-xs text-muted-foreground flex gap-2">
              <Info className="h-4 w-4 text-primary shrink-0 mt-0.5" />
              <div>
                Text partitioning and semantic embedding generation runs asynchronously upon uploading in subsequent phases.
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
