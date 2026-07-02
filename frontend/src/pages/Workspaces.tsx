import { useState } from 'react'
import {
  useWorkspaces,
  useCreateWorkspace,
  useUpdateWorkspace,
  useDeleteWorkspace,
} from '@/hooks/useWorkspaces'
import type { Workspace } from '@/types'
import {
  FolderPlus,
  Layers,
  Calendar,
  Database,
  FileText,
  Trash2,
  Edit2,
  AlertCircle,
  X,
  Plus,
  Loader2,
} from 'lucide-react'

export default function Workspaces() {
  const { data: workspaces, isLoading, isError, error } = useWorkspaces()

  // Mutations
  const createMutation = useCreateWorkspace()
  const updateMutation = useUpdateWorkspace()
  const deleteMutation = useDeleteWorkspace()

  // State Management
  const [isCreateOpen, setIsCreateOpen] = useState(false)
  const [editWorkspace, setEditWorkspace] = useState<Workspace | null>(null)
  const [deleteWorkspace, setDeleteWorkspace] = useState<Workspace | null>(null)

  // Form Fields
  const [name, setName] = useState('')
  const [description, setDescription] = useState('')
  const [formError, setFormError] = useState('')

  const handleOpenCreate = () => {
    setName('')
    setDescription('')
    setFormError('')
    setIsCreateOpen(true)
  }

  const handleOpenEdit = (ws: Workspace) => {
    setName(ws.name)
    setDescription(ws.description)
    setFormError('')
    setEditWorkspace(ws)
  }

  const handleCreateSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!name.trim()) {
      setFormError('Workspace name is required')
      return
    }
    try {
      await createMutation.mutateAsync({ name, description })
      setIsCreateOpen(false)
    } catch (err: any) {
      setFormError(err.response?.data?.message || 'Failed to create workspace')
    }
  }

  const handleEditSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!name.trim()) {
      setFormError('Workspace name is required')
      return
    }
    if (!editWorkspace) return
    try {
      await updateMutation.mutateAsync({
        id: editWorkspace.id,
        name,
        description,
      })
      setEditWorkspace(null)
    } catch (err: any) {
      setFormError(err.response?.data?.message || 'Failed to update workspace')
    }
  }

  const handleDeleteConfirm = async () => {
    if (!deleteWorkspace) return
    try {
      await deleteMutation.mutateAsync(deleteWorkspace.id)
      setDeleteWorkspace(null)
    } catch (err: any) {
      alert('Failed to delete workspace')
    }
  }

  return (
    <div className="p-6 space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">Workspaces</h1>
          <p className="text-muted-foreground mt-1">
            Isolate and manage semantic indexes and documents across logical boundaries.
          </p>
        </div>
        <button
          onClick={handleOpenCreate}
          className="inline-flex items-center gap-2 rounded-lg bg-primary px-4 py-2 text-sm font-semibold text-primary-foreground shadow hover:bg-primary/95 transition-all"
        >
          <FolderPlus className="h-4 w-4" />
          Create Workspace
        </button>
      </div>

      {/* Loading State */}
      {isLoading && (
        <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
          {[1, 2, 3].map((n) => (
            <div
              key={n}
              className="rounded-xl border border-border bg-card p-5 space-y-4 shadow-sm animate-pulse"
            >
              <div className="flex justify-between items-start">
                <div className="h-10 w-10 rounded-lg bg-muted" />
                <div className="h-5 w-20 rounded bg-muted" />
              </div>
              <div className="space-y-2">
                <div className="h-5 w-3/4 rounded bg-muted" />
                <div className="h-3 w-full rounded bg-muted" />
              </div>
              <div className="h-10 border-t border-border pt-4 grid grid-cols-2 gap-4">
                <div className="h-4 w-16 bg-muted rounded" />
                <div className="h-4 w-16 bg-muted rounded" />
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Error State */}
      {isError && (
        <div className="rounded-lg border border-destructive/20 bg-destructive/10 p-4 text-sm text-destructive flex items-start gap-3">
          <AlertCircle className="h-5 w-5 shrink-0" />
          <div>
            <span className="font-bold">Error loading workspaces:</span>{' '}
            {error instanceof Error ? error.message : 'Unknown network failure'}
          </div>
        </div>
      )}

      {/* Empty State */}
      {!isLoading && !isError && workspaces?.length === 0 && (
        <div className="border border-dashed border-border rounded-xl p-12 text-center max-w-lg mx-auto space-y-4">
          <div className="rounded-full bg-muted p-4 w-fit mx-auto">
            <Layers className="h-8 w-8 text-muted-foreground" />
          </div>
          <div>
            <h3 className="font-bold text-lg">No workspaces found</h3>
            <p className="text-sm text-muted-foreground mt-1">
              Create your first logical workspace to begin indexing and querying files.
            </p>
          </div>
          <button
            onClick={handleOpenCreate}
            className="inline-flex items-center gap-2 rounded-lg bg-primary px-4 py-2 text-sm font-semibold text-primary-foreground shadow hover:bg-primary/95 transition-all"
          >
            <Plus className="h-4 w-4" />
            Add First Workspace
          </button>
        </div>
      )}

      {/* Grid List */}
      {!isLoading && !isError && workspaces && workspaces.length > 0 && (
        <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
          {workspaces.map((ws) => (
            <div
              key={ws.id}
              className="rounded-xl border border-border bg-card p-5 shadow-sm space-y-4 hover:border-foreground/10 transition-all flex flex-col justify-between"
            >
              <div className="space-y-3">
                <div className="flex items-start justify-between">
                  <div className="rounded-lg p-2.5 bg-primary/10 text-primary">
                    <Layers className="h-5 w-5" />
                  </div>
                  <div className="flex gap-1">
                    <button
                      onClick={() => handleOpenEdit(ws)}
                      className="p-1 rounded text-muted-foreground hover:text-foreground hover:bg-secondary/60 transition-colors"
                      title="Edit Workspace"
                    >
                      <Edit2 className="h-3.5 w-3.5" />
                    </button>
                    <button
                      onClick={() => setDeleteWorkspace(ws)}
                      className="p-1 rounded text-muted-foreground hover:text-destructive hover:bg-destructive/10 transition-colors"
                      title="Delete Workspace"
                    >
                      <Trash2 className="h-3.5 w-3.5" />
                    </button>
                  </div>
                </div>

                <div>
                  <h3 className="font-bold text-lg">{ws.name}</h3>
                  <p className="text-sm text-muted-foreground mt-1 line-clamp-2">
                    {ws.description || 'No description provided.'}
                  </p>
                </div>
              </div>

              <div className="space-y-3 pt-4 border-t border-border mt-4">
                <div className="grid grid-cols-2 gap-4 text-xs">
                  <div className="flex items-center gap-1.5 text-muted-foreground">
                    <FileText className="h-3.5 w-3.5" />
                    <span>{ws.totalDocuments} Docs</span>
                  </div>
                  <div className="flex items-center gap-1.5 text-muted-foreground">
                    <Database className="h-3.5 w-3.5" />
                    <span>{ws.totalVectors} Vectors</span>
                  </div>
                </div>

                <div className="flex items-center gap-1.5 text-[10px] text-muted-foreground">
                  <Calendar className="h-3 w-3" />
                  <span>Created: {new Date(ws.createdAt).toLocaleDateString()}</span>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* CREATE WORKSPACE DIALOG */}
      {isCreateOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm p-4">
          <div className="w-full max-w-md rounded-xl border border-border bg-card p-6 shadow-lg space-y-4 animate-in fade-in zoom-in-95 duration-150">
            <div className="flex items-center justify-between">
              <h2 className="text-xl font-bold tracking-tight">Create Workspace</h2>
              <button
                onClick={() => setIsCreateOpen(false)}
                className="rounded-md p-1 hover:bg-secondary text-muted-foreground hover:text-foreground"
              >
                <X className="h-4 w-4" />
              </button>
            </div>

            <form onSubmit={handleCreateSubmit} className="space-y-4">
              <div className="space-y-1">
                <label className="text-xs font-semibold block">Workspace Name</label>
                <input
                  type="text"
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  placeholder="e.g. Finance Data Index"
                  className="w-full text-sm border border-input rounded-md px-3 py-2 bg-background focus:outline-none focus:ring-1 focus:ring-primary"
                />
              </div>

              <div className="space-y-1">
                <label className="text-xs font-semibold block">Description</label>
                <textarea
                  value={description}
                  onChange={(e) => setDescription(e.target.value)}
                  placeholder="Details about documents indexed here..."
                  rows={3}
                  className="w-full text-sm border border-input rounded-md px-3 py-2 bg-background focus:outline-none focus:ring-1 focus:ring-primary"
                />
              </div>

              {formError && (
                <div className="rounded bg-destructive/10 text-destructive text-xs p-2.5 flex items-center gap-2 border border-destructive/20">
                  <AlertCircle className="h-4 w-4 shrink-0" />
                  <span>{formError}</span>
                </div>
              )}

              <div className="flex justify-end gap-3 pt-2">
                <button
                  type="button"
                  onClick={() => setIsCreateOpen(false)}
                  className="px-4 py-2 text-sm font-semibold rounded-lg border border-border hover:bg-secondary transition-colors"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={createMutation.isPending}
                  className="inline-flex items-center gap-1.5 px-4 py-2 text-sm font-semibold rounded-lg bg-primary text-primary-foreground shadow hover:bg-primary/95 transition-colors disabled:opacity-50"
                >
                  {createMutation.isPending && <Loader2 className="h-3.5 w-3.5 animate-spin" />}
                  Create
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* EDIT WORKSPACE DIALOG */}
      {editWorkspace && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm p-4">
          <div className="w-full max-w-md rounded-xl border border-border bg-card p-6 shadow-lg space-y-4 animate-in fade-in zoom-in-95 duration-150">
            <div className="flex items-center justify-between">
              <h2 className="text-xl font-bold tracking-tight">Edit Workspace</h2>
              <button
                onClick={() => setEditWorkspace(null)}
                className="rounded-md p-1 hover:bg-secondary text-muted-foreground hover:text-foreground"
              >
                <X className="h-4 w-4" />
              </button>
            </div>

            <form onSubmit={handleEditSubmit} className="space-y-4">
              <div className="space-y-1">
                <label className="text-xs font-semibold block">Workspace Name</label>
                <input
                  type="text"
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  placeholder="e.g. Updated Workspace Name"
                  className="w-full text-sm border border-input rounded-md px-3 py-2 bg-background focus:outline-none focus:ring-1 focus:ring-primary"
                />
              </div>

              <div className="space-y-1">
                <label className="text-xs font-semibold block">Description</label>
                <textarea
                  value={description}
                  onChange={(e) => setDescription(e.target.value)}
                  placeholder="Details about documents..."
                  rows={3}
                  className="w-full text-sm border border-input rounded-md px-3 py-2 bg-background focus:outline-none focus:ring-1 focus:ring-primary"
                />
              </div>

              {formError && (
                <div className="rounded bg-destructive/10 text-destructive text-xs p-2.5 flex items-center gap-2 border border-destructive/20">
                  <AlertCircle className="h-4 w-4 shrink-0" />
                  <span>{formError}</span>
                </div>
              )}

              <div className="flex justify-end gap-3 pt-2">
                <button
                  type="button"
                  onClick={() => setEditWorkspace(null)}
                  className="px-4 py-2 text-sm font-semibold rounded-lg border border-border hover:bg-secondary transition-colors"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={updateMutation.isPending}
                  className="inline-flex items-center gap-1.5 px-4 py-2 text-sm font-semibold rounded-lg bg-primary text-primary-foreground shadow hover:bg-primary/95 transition-colors disabled:opacity-50"
                >
                  {updateMutation.isPending && <Loader2 className="h-3.5 w-3.5 animate-spin" />}
                  Save Changes
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* DELETE CONFIRMATION DIALOG */}
      {deleteWorkspace && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm p-4">
          <div className="w-full max-w-sm rounded-xl border border-border bg-card p-6 shadow-lg space-y-4 animate-in fade-in zoom-in-95 duration-150">
            <h2 className="text-lg font-bold tracking-tight text-destructive">Delete Workspace?</h2>
            <p className="text-sm text-muted-foreground">
              Are you sure you want to delete workspace <span className="font-semibold text-foreground">"{deleteWorkspace.name}"</span>?
              All document indexes and vectors stored within will be permanently removed.
            </p>

            <div className="flex justify-end gap-3 pt-2">
              <button
                type="button"
                onClick={() => setDeleteWorkspace(null)}
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
