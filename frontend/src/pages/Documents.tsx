import { useState, useRef } from 'react'
import { Link } from 'react-router-dom'
import { useWorkspaces } from '@/hooks/useWorkspaces'
import { useDocuments, useUploadDocument, useDeleteDocument, useRetryDocument } from '@/hooks/useDocuments'
import type { Document } from '@/types'
import {
  FileUp,
  FileText,
  CheckCircle2,
  Loader2,
  Trash2,
  Eye,
  AlertCircle,
  FolderOpen,
  Layers,
  X,
  RefreshCw,
} from 'lucide-react'

export default function Documents() {
  const { data: workspaces, isLoading: isWorkspacesLoading } = useWorkspaces()
  const [selectedWorkspaceId, setSelectedWorkspaceId] = useState<number>(0)

  // Auto-select first workspace when loaded
  if (workspaces && workspaces.length > 0 && selectedWorkspaceId === 0) {
    setSelectedWorkspaceId(workspaces[0].id)
  }

  const { data: documents, isLoading, isError, error } = useDocuments(selectedWorkspaceId)
  const uploadMutation = useUploadDocument()
  const deleteMutation = useDeleteDocument()
  const retryMutation = useRetryDocument()

  // Drag and Drop State
  const [dragActive, setDragActive] = useState(false)
  const [selectedFile, setSelectedFile] = useState<File | null>(null)
  const [uploadError, setUploadError] = useState('')
  const fileInputRef = useRef<HTMLInputElement>(null)

  // Dialogs State
  const [deleteDoc, setDeleteDoc] = useState<Document | null>(null)

  const handleDrag = (e: React.DragEvent) => {
    e.preventDefault()
    e.stopPropagation()
    if (e.type === 'dragenter' || e.type === 'dragover') {
      setDragActive(true)
    } else if (e.type === 'dragleave') {
      setDragActive(false)
    }
  }

  const validateAndSetFile = (file: File) => {
    const ext = file.name.split('.').pop()?.toLowerCase()
    const allowed = ['pdf', 'docx', 'txt', 'md']
    if (!ext || !allowed.includes(ext)) {
      setUploadError('Unsupported file type. Please upload PDF, DOCX, TXT, or MD files.')
      setSelectedFile(null)
      return
    }
    if (file.size > 10 * 1024 * 1024) {
      setUploadError('File size exceeds the 10MB limit.')
      setSelectedFile(null)
      return
    }
    setUploadError('')
    setSelectedFile(file)
  }

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault()
    e.stopPropagation()
    setDragActive(false)
    if (e.dataTransfer.files && e.dataTransfer.files[0]) {
      validateAndSetFile(e.dataTransfer.files[0])
    }
  }

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    e.preventDefault()
    if (e.target.files && e.target.files[0]) {
      validateAndSetFile(e.target.files[0])
    }
  }

  const triggerFileInput = () => {
    fileInputRef.current?.click()
  }

  const handleUploadSubmit = async () => {
    if (!selectedFile || selectedWorkspaceId === 0) return
    try {
      await uploadMutation.mutateAsync({
        workspaceId: selectedWorkspaceId,
        file: selectedFile,
      })
      setSelectedFile(null)
    } catch (err: any) {
      setUploadError(err.response?.data?.message || 'Failed to upload document')
    }
  }

  const handleDeleteConfirm = async () => {
    if (!deleteDoc) return
    try {
      await deleteMutation.mutateAsync({
        id: deleteDoc.id,
        workspaceId: selectedWorkspaceId,
      })
      setDeleteDoc(null)
    } catch (err) {
      alert('Failed to delete document')
    }
  }

  const formatBytes = (bytes: number) => {
    if (bytes === 0) return '0 Bytes'
    const k = 1024
    const sizes = ['Bytes', 'KB', 'MB']
    const i = Math.floor(Math.log(bytes) / Math.log(k))
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
  }

  return (
    <div className="p-6 space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">Documents</h1>
          <p className="text-muted-foreground mt-1">
            Upload text materials to partition them into semantic slices and test retrieval quality.
          </p>
        </div>

        {/* Workspace Selector */}
        <div className="flex items-center gap-2">
          <Layers className="h-4 w-4 text-muted-foreground" />
          <select
            value={selectedWorkspaceId}
            onChange={(e) => setSelectedWorkspaceId(Number(e.target.value))}
            className="text-sm border border-input rounded-lg px-3 py-2 bg-card focus:outline-none focus:ring-1 focus:ring-primary min-w-[200px]"
          >
            {isWorkspacesLoading && <option>Loading workspaces...</option>}
            {workspaces && workspaces.length === 0 && <option>No workspaces created</option>}
            {workspaces?.map((ws) => (
              <option key={ws.id} value={ws.id}>
                {ws.name}
              </option>
            ))}
          </select>
        </div>
      </div>

      {workspaces && workspaces.length === 0 ? (
        <div className="border border-dashed border-border rounded-xl p-12 text-center max-w-lg mx-auto space-y-4">
          <FolderOpen className="mx-auto h-12 w-12 text-muted-foreground" />
          <div>
            <h3 className="font-bold text-lg">Create a workspace first</h3>
            <p className="text-sm text-muted-foreground mt-1">
              Documents must be bound to a logical workspace index. Setup a workspace under the Workspaces tab.
            </p>
          </div>
          <Link
            to="/workspaces"
            className="inline-flex items-center gap-2 rounded-lg bg-primary px-4 py-2 text-sm font-semibold text-primary-foreground shadow hover:bg-primary/95"
          >
            Go to Workspaces
          </Link>
        </div>
      ) : (
        <div className="grid gap-6 lg:grid-cols-3">
          {/* Drag and Drop Panel */}
          <div className="rounded-xl border border-border bg-card p-6 shadow-sm space-y-4 lg:col-span-1 h-fit">
            <h2 className="text-lg font-bold tracking-tight">Upload Document</h2>

            <div
              onDragEnter={handleDrag}
              onDragOver={handleDrag}
              onDragLeave={handleDrag}
              onDrop={handleDrop}
              onClick={triggerFileInput}
              className={`border-2 border-dashed rounded-lg p-8 text-center cursor-pointer transition-colors ${
                dragActive
                  ? 'border-primary bg-primary/5'
                  : 'border-border hover:bg-muted/10'
              }`}
            >
              <input
                ref={fileInputRef}
                type="file"
                onChange={handleChange}
                accept=".pdf,.docx,.txt,.md"
                className="hidden"
              />
              <FileUp className="mx-auto h-10 w-10 text-muted-foreground mb-3" />
              <span className="text-sm font-semibold block">Drag & drop your file here</span>
              <span className="text-xs text-muted-foreground block mt-1">or click to browse local files</span>
              <span className="text-[10px] text-muted-foreground/60 block mt-2">
                Supported types: PDF, DOCX, TXT, MD (Max 10MB)
              </span>
            </div>

            {selectedFile && (
              <div className="rounded-lg bg-secondary/40 border border-border p-3 flex items-center justify-between gap-3">
                <div className="flex items-center gap-2.5 min-w-0">
                  <FileText className="h-5 w-5 text-primary shrink-0" />
                  <div className="min-w-0">
                    <div className="text-sm font-medium truncate">{selectedFile.name}</div>
                    <div className="text-xs text-muted-foreground">{formatBytes(selectedFile.size)}</div>
                  </div>
                </div>
                <button
                  onClick={() => setSelectedFile(null)}
                  className="p-1 hover:bg-secondary rounded text-muted-foreground hover:text-foreground"
                >
                  <X className="h-4 w-4" />
                </button>
              </div>
            )}

            {uploadError && (
              <div className="rounded bg-destructive/10 text-destructive text-xs p-2.5 flex items-center gap-2 border border-destructive/20">
                <AlertCircle className="h-4 w-4 shrink-0" />
                <span>{uploadError}</span>
              </div>
            )}

            <button
              onClick={handleUploadSubmit}
              disabled={!selectedFile || uploadMutation.isPending}
              className="w-full inline-flex justify-center items-center gap-2 rounded-lg bg-primary px-4 py-2 text-sm font-semibold text-primary-foreground shadow hover:bg-primary/95 transition-all disabled:opacity-50"
            >
              {uploadMutation.isPending && <Loader2 className="h-3.5 w-3.5 animate-spin" />}
              Start Indexing
            </button>
          </div>

          {/* Documents Table */}
          <div className="rounded-xl border border-border bg-card p-6 shadow-sm lg:col-span-2 space-y-4">
            <h2 className="text-lg font-bold tracking-tight">Workspace Documents</h2>

            {isLoading && (
              <div className="space-y-3">
                {[1, 2].map((n) => (
                  <div key={n} className="h-14 rounded-lg bg-muted animate-pulse" />
                ))}
              </div>
            )}

            {isError && (
              <div className="rounded-lg border border-destructive/20 bg-destructive/10 p-4 text-sm text-destructive flex items-start gap-3">
                <AlertCircle className="h-5 w-5 shrink-0" />
                <div>
                  <span className="font-bold">Error loading documents:</span>{' '}
                  {error instanceof Error ? error.message : 'Unknown database error'}
                </div>
              </div>
            )}

            {!isLoading && !isError && documents?.length === 0 && (
              <div className="border border-dashed border-border rounded-lg p-12 text-center text-sm text-muted-foreground space-y-2">
                <FileText className="mx-auto h-8 w-8 text-muted-foreground/65" />
                <div className="font-semibold text-foreground">No documents uploaded</div>
                <div>Upload PDF or Text files to begin indexing documents in this workspace.</div>
              </div>
            )}

            {!isLoading && !isError && documents && documents.length > 0 && (
              <div className="overflow-x-auto border border-border rounded-lg">
                <table className="w-full text-sm text-left border-collapse">
                  <thead>
                    <tr className="bg-muted/40 border-b border-border">
                      <th className="px-4 py-3 font-semibold text-xs text-muted-foreground">Document Details</th>
                      <th className="px-4 py-3 font-semibold text-xs text-muted-foreground">Chunks</th>
                      <th className="px-4 py-3 font-semibold text-xs text-muted-foreground">Status</th>
                      <th className="px-4 py-3 font-semibold text-xs text-muted-foreground text-right">Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {documents.map((doc) => (
                      <tr key={doc.id} className="border-b border-border hover:bg-muted/10 last:border-0">
                        <td className="px-4 py-3">
                          <div className="flex items-center gap-2">
                            <FileText className="h-4 w-4 text-primary shrink-0" />
                            <div className="min-w-0">
                              <div className="font-semibold truncate max-w-xs">{doc.title}</div>
                              <div className="text-[10px] text-muted-foreground">
                                {formatBytes(doc.size)} • Type: {doc.fileType}
                              </div>
                            </div>
                          </div>
                        </td>
                        <td className="px-4 py-3 font-mono text-xs">{doc.totalChunks} chunks</td>
                        <td className="px-4 py-3">
                          {(() => {
                            switch (doc.processingStatus) {
                              case 'READY':
                                return (
                                  <span className="inline-flex items-center gap-1 rounded-full bg-green-500/10 px-2.5 py-0.5 text-xs font-semibold text-green-500 border border-green-500/20">
                                    <CheckCircle2 className="h-3.5 w-3.5" />
                                    Ready (100%)
                                  </span>
                                )
                              case 'FAILED':
                                return (
                                  <span 
                                    className="inline-flex items-center gap-1 rounded-full bg-red-500/10 px-2.5 py-0.5 text-xs font-semibold text-red-500 border border-red-500/20 cursor-help"
                                    title={doc.failureReason || 'An unknown error occurred during text extraction or indexing.'}
                                  >
                                    <AlertCircle className="h-3.5 w-3.5" />
                                    Failed
                                  </span>
                                )
                              case 'UPLOADED':
                                return (
                                  <span className="inline-flex items-center gap-1 rounded-full bg-slate-500/10 px-2.5 py-0.5 text-xs font-semibold text-slate-400 border border-slate-500/20">
                                    <Loader2 className="h-3.5 w-3.5 animate-spin" />
                                    Uploaded (10%)
                                  </span>
                                )
                              case 'VALIDATING':
                                return (
                                  <span className="inline-flex items-center gap-1 rounded-full bg-orange-500/10 px-2.5 py-0.5 text-xs font-semibold text-orange-400 border border-orange-500/20">
                                    <Loader2 className="h-3.5 w-3.5 animate-spin" />
                                    Validating (20%)
                                  </span>
                                )
                              case 'EXTRACTING_TEXT':
                                return (
                                  <span className="inline-flex items-center gap-1 rounded-full bg-yellow-500/10 px-2.5 py-0.5 text-xs font-semibold text-yellow-400 border border-yellow-500/20">
                                    <Loader2 className="h-3.5 w-3.5 animate-spin" />
                                    Extracting (35%)
                                  </span>
                                )
                              case 'CLEANING_TEXT':
                                return (
                                  <span className="inline-flex items-center gap-1 rounded-full bg-amber-500/10 px-2.5 py-0.5 text-xs font-semibold text-amber-400 border border-amber-500/20">
                                    <Loader2 className="h-3.5 w-3.5 animate-spin" />
                                    Cleaning (50%)
                                  </span>
                                )
                              case 'CHUNKING':
                                return (
                                  <span className="inline-flex items-center gap-1 rounded-full bg-indigo-500/10 px-2.5 py-0.5 text-xs font-semibold text-indigo-400 border border-indigo-500/20">
                                    <Loader2 className="h-3.5 w-3.5 animate-spin" />
                                    Chunking (65%)
                                  </span>
                                )
                              case 'GENERATING_EMBEDDINGS':
                                return (
                                  <span className="inline-flex items-center gap-1 rounded-full bg-cyan-500/10 px-2.5 py-0.5 text-xs font-semibold text-cyan-400 border border-cyan-500/20">
                                    <Loader2 className="h-3.5 w-3.5 animate-spin" />
                                    Vectorizing (80%)
                                  </span>
                                )
                              case 'INDEXING':
                                return (
                                  <span className="inline-flex items-center gap-1 rounded-full bg-fuchsia-500/10 px-2.5 py-0.5 text-xs font-semibold text-fuchsia-400 border border-fuchsia-500/20">
                                    <Loader2 className="h-3.5 w-3.5 animate-spin" />
                                    Indexing (95%)
                                  </span>
                                )
                              default:
                                return (
                                  <span className="inline-flex items-center gap-1 rounded-full bg-muted px-2.5 py-0.5 text-xs font-semibold text-muted-foreground">
                                    {doc.processingStatus}
                                  </span>
                                )
                            }
                          })()}
                        </td>
                        <td className="px-4 py-3 text-right">
                          <div className="flex justify-end gap-1.5">
                            {doc.processingStatus === 'FAILED' && (
                              <button
                                onClick={() => retryMutation.mutate({ id: doc.id, workspaceId: selectedWorkspaceId })}
                                disabled={retryMutation.isPending}
                                className="rounded p-1 text-amber-500 hover:text-amber-400 hover:bg-amber-500/10 transition-all disabled:opacity-50"
                                title="Retry processing"
                              >
                                <RefreshCw className={`h-4 w-4 ${retryMutation.isPending ? 'animate-spin' : ''}`} />
                              </button>
                            )}
                            <Link
                              to={`/documents/${doc.id}`}
                              className="rounded p-1 text-muted-foreground hover:text-foreground hover:bg-secondary/60 transition-all"
                              title="View Details"
                            >
                              <Eye className="h-4 w-4" />
                            </Link>
                            <button
                              onClick={() => setDeleteDoc(doc)}
                              className="rounded p-1 text-muted-foreground hover:text-destructive hover:bg-destructive/10 transition-all"
                              title="Delete Document"
                            >
                              <Trash2 className="h-4 w-4" />
                            </button>
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </div>
      )}

      {/* DELETE CONFIRMATION DIALOG */}
      {deleteDoc && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm p-4">
          <div className="w-full max-w-sm rounded-xl border border-border bg-card p-6 shadow-lg space-y-4 animate-in fade-in zoom-in-95 duration-150">
            <h2 className="text-lg font-bold tracking-tight text-destructive">Delete Document?</h2>
            <p className="text-sm text-muted-foreground">
              Are you sure you want to delete document <span className="font-semibold text-foreground">"{deleteDoc.title}"</span>?
              All text chunks and vector embeddings associated will be permanently removed.
            </p>

            <div className="flex justify-end gap-3 pt-2">
              <button
                type="button"
                onClick={() => setDeleteDoc(null)}
                className="px-4 py-2 text-sm font-semibold rounded-lg border border-border hover:bg-secondary transition-colors"
              >
                Cancel
              </button>
              <button
                onClick={handleDeleteConfirm}
                disabled={deleteMutation.isPending}
                className="inline-flex items-center gap-1.5 px-4 py-2 text-sm font-semibold rounded-lg bg-destructive text-destructive-foreground shadow hover:bg-destructive/90 transition-colors disabled:opacity-50"
              >
                {deleteMutation.isPending && <Loader2 className="h-3.5 w-3.5 animate-spin" />}
                Delete
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
