import { useEffect } from 'react'
import { useParams, Link, useSearchParams } from 'react-router-dom'
import { useDocumentById, useDocumentChunks } from '@/hooks/useDocuments'
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
  Loader2,
  BookOpen
} from 'lucide-react'

export default function DocumentDetails() {
  const { id } = useParams<{ id: string }>()
  const documentId = Number(id)
  const [searchParams] = useSearchParams()
  const highlightChunkId = searchParams.get('highlightChunkId') ? Number(searchParams.get('highlightChunkId')) : null

  const { data: doc, isLoading: isDocLoading, isError: isDocError, error: docError } = useDocumentById(documentId)
  const { data: chunks, isLoading: isChunksLoading } = useDocumentChunks(documentId)

  useEffect(() => {
    if (chunks && highlightChunkId) {
      const timer = setTimeout(() => {
        const element = document.getElementById(`chunk-${highlightChunkId}`)
        if (element) {
          element.scrollIntoView({ behavior: 'smooth', block: 'center' })
        }
      }, 500)
      return () => clearTimeout(timer)
    }
  }, [chunks, highlightChunkId])

  const formatBytes = (bytes: number) => {
    if (bytes === 0) return '0 Bytes'
    const k = 1024
    const sizes = ['Bytes', 'KB', 'MB']
    const i = Math.floor(Math.log(bytes) / Math.log(k))
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
  }

  const isLoading = isDocLoading || isChunksLoading
  const isError = isDocError

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
          <h1 className="text-2xl font-bold tracking-tight">Document Viewer</h1>
          <p className="text-xs text-muted-foreground mt-0.5">
            View original document content, segments, and metadata logs.
          </p>
        </div>
      </div>

      {isLoading && (
        <div className="rounded-xl border border-border bg-card p-6 space-y-4 animate-pulse">
          <div className="h-6 w-1/3 bg-muted rounded" />
          <div className="grid gap-4 md:grid-cols-3">
            <div className="md:col-span-2 h-48 bg-muted rounded" />
            <div className="md:col-span-1 h-48 bg-muted rounded" />
          </div>
        </div>
      )}

      {isError && (
        <div className="rounded-lg border border-destructive/20 bg-destructive/10 p-4 text-sm text-destructive flex items-start gap-3">
          <AlertCircle className="h-5 w-5 shrink-0" />
          <div>
            <span className="font-bold">Error loading document:</span>{' '}
            {docError instanceof Error ? docError.message : 'Unknown database error'}
          </div>
        </div>
      )}

      {!isLoading && !isError && doc && (
        <div className="grid gap-6 md:grid-cols-3">
          {/* Left panel: Document text chunks reader */}
          <div className="rounded-xl border border-border bg-card p-6 shadow-sm space-y-4 md:col-span-2 flex flex-col h-[65vh]">
            <h3 className="font-bold text-sm flex items-center gap-2 border-b border-border pb-3">
              <BookOpen className="h-4 w-4 text-violet-400" />
              Document Text Content
            </h3>
            <div className="flex-1 overflow-y-auto space-y-4 pr-2 scrollbar-thin">
              {doc.processingStatus !== 'READY' ? (
                <div className="flex flex-col items-center justify-center h-full text-center p-6 gap-3">
                  <Loader2 className="h-8 w-8 animate-spin text-primary" />
                  <span className="text-sm font-semibold text-muted-foreground">
                    Document pipeline status: {doc.processingStatus}...
                  </span>
                  <span className="text-xs text-muted-foreground/60 max-w-xs">
                    Text is currently being extracted and indexed into the vector store.
                  </span>
                </div>
              ) : chunks && chunks.length > 0 ? (
                chunks.map((chunk: any) => (
                  <div
                    key={chunk.id}
                    id={`chunk-${chunk.id}`}
                    className={`p-4 rounded-xl border transition-all duration-300 ${
                      highlightChunkId === chunk.id
                        ? 'border-violet-500 bg-violet-500/10 shadow-md scroll-mt-20 scale-[1.01]'
                        : 'border-border bg-muted/5 hover:bg-muted/15'
                    }`}
                  >
                    <div className="flex justify-between items-center text-3xs font-mono text-muted-foreground mb-2 pb-1.5 border-b border-border/40">
                      <span>SEGMENT #{chunk.chunkIndex + 1}</span>
                      <span>Chars: {chunk.startChar} - {chunk.endChar}</span>
                    </div>
                    <p className="text-sm leading-relaxed text-foreground/90 whitespace-pre-wrap select-text">
                      {chunk.content}
                    </p>
                  </div>
                ))
              ) : (
                <div className="flex flex-col items-center justify-center h-full text-sm text-muted-foreground p-6">
                  No segments or chunks found for this document. Try retrying the document processing.
                </div>
              )}
            </div>
          </div>

          {/* Right panel: Metadata & telemetry details */}
          <div className="space-y-6 md:col-span-1">
            {/* Main Info Card */}
            <div className="rounded-xl border border-border bg-card p-6 shadow-sm space-y-6">
              <div className="flex items-start gap-4">
                <div className="rounded-xl p-3.5 bg-primary/10 text-primary">
                  <FileText className="h-8 w-8" />
                </div>
                <div className="min-w-0 flex-1">
                  <h2 className="text-lg font-bold truncate" title={doc.title}>{doc.title}</h2>
                  <div className="flex items-center gap-2 mt-1.5">
                    <span className={`inline-flex items-center gap-1 rounded-full px-2 py-0.5 text-2xs font-semibold border ${
                      doc.processingStatus === 'READY'
                        ? 'bg-green-500/10 text-green-500 border-green-500/20'
                        : doc.processingStatus === 'FAILED'
                        ? 'bg-red-500/10 text-red-500 border-red-500/20'
                        : 'bg-amber-500/10 text-amber-500 border-amber-500/20 animate-pulse'
                    }`}>
                      <CheckCircle2 className="h-3 w-3" />
                      {doc.processingStatus}
                    </span>
                    <span className="text-xs text-muted-foreground">ID: {doc.id}</span>
                  </div>
                </div>
              </div>

              {/* Metadata Grid */}
              <div className="grid gap-4 pt-4 border-t border-border">
                <div className="flex items-center gap-3">
                  <HardDrive className="h-4 w-4 text-muted-foreground shrink-0" />
                  <div>
                    <span className="text-[10px] text-muted-foreground block">File Size</span>
                    <span className="text-xs font-semibold">{formatBytes(doc.size)}</span>
                  </div>
                </div>

                <div className="flex items-center gap-3">
                  <Layers className="h-4 w-4 text-muted-foreground shrink-0" />
                  <div>
                    <span className="text-[10px] text-muted-foreground block">Format Extension</span>
                    <span className="text-xs font-semibold">{doc.fileType}</span>
                  </div>
                </div>

                <div className="flex items-center gap-3">
                  <Calendar className="h-4 w-4 text-muted-foreground shrink-0" />
                  <div>
                    <span className="text-[10px] text-muted-foreground block">Upload Time</span>
                    <span className="text-xs font-semibold">{new Date(doc.uploadTime).toLocaleString()}</span>
                  </div>
                </div>

                <div className="flex items-center gap-3">
                  <Clock className="h-4 w-4 text-muted-foreground shrink-0" />
                  <div>
                    <span className="text-[10px] text-muted-foreground block">Target Workspace ID</span>
                    <span className="text-xs font-semibold">Workspace #{doc.workspaceId}</span>
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
            <div className="rounded-xl border border-border bg-card p-6 shadow-sm space-y-6">
              <h3 className="font-bold text-sm flex items-center gap-2 border-b border-border pb-3">
                <Database className="h-5 w-5 text-muted-foreground" />
                Index Telemetry
              </h3>

              <div className="space-y-4">
                <div className="flex items-center justify-between text-xs">
                  <span className="text-muted-foreground">Total Chunks</span>
                  <span className="font-bold font-mono">{doc.totalChunks}</span>
                </div>
                <div className="flex items-center justify-between text-xs">
                  <span className="text-muted-foreground">Vector Dimensions</span>
                  <span className="font-semibold font-mono">384 (nomic-embed)</span>
                </div>
              </div>

              <div className="rounded-lg border border-primary/20 bg-primary/5 p-4 text-xs text-muted-foreground flex gap-2">
                <Info className="h-4 w-4 text-primary shrink-0 mt-0.5" />
                <div>
                  Semantic vectors are calculated using lightweight embeddings, allowing for fast retrieval on local CPU models.
                </div>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
