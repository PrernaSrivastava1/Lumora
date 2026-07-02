import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { documentService } from '@/services/documentService'

export function useDocuments(workspaceId: number) {
  return useQuery({
    queryKey: ['documents', workspaceId],
    queryFn: () => documentService.getDocuments(workspaceId).then((res) => res.data),
    enabled: !!workspaceId,
  })
}

export function useDocumentById(id: number) {
  return useQuery({
    queryKey: ['document', id],
    queryFn: () => documentService.getDocumentById(id).then((res) => res.data),
    enabled: !!id,
  })
}

export function useUploadDocument() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ workspaceId, file }: { workspaceId: number; file: File }) =>
      documentService.uploadDocument(workspaceId, file),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ['documents', variables.workspaceId] })
      queryClient.invalidateQueries({ queryKey: ['workspaces'] }) // invalidate workspaces to reflect document count changes
    },
  })
}

export function useDeleteDocument() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ id }: { id: number; workspaceId: number }) =>
      documentService.deleteDocument(id),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ['documents', variables.workspaceId] })
      queryClient.invalidateQueries({ queryKey: ['workspaces'] })
    },
  })
}
