import apiClient from './api'
import type { ApiResponse, Document } from '@/types'

export const documentService = {
  getDocuments: async (workspaceId: number): Promise<ApiResponse<Document[]>> => {
    const response = await apiClient.get<ApiResponse<Document[]>>('/documents', {
      params: { workspaceId },
    })
    return response.data
  },

  getDocumentById: async (id: number): Promise<ApiResponse<Document>> => {
    const response = await apiClient.get<ApiResponse<Document>>(`/documents/${id}`)
    return response.data
  },

  uploadDocument: async (workspaceId: number, file: File): Promise<ApiResponse<Document>> => {
    const formData = new FormData()
    formData.append('workspaceId', workspaceId.toString())
    formData.append('file', file)
    const response = await apiClient.post<ApiResponse<Document>>('/documents', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    })
    return response.data
  },

  deleteDocument: async (id: number): Promise<ApiResponse<void>> => {
    const response = await apiClient.delete<ApiResponse<void>>(`/documents/${id}`)
    return response.data
  },

  retryDocument: async (id: number): Promise<ApiResponse<void>> => {
    const response = await apiClient.post<ApiResponse<void>>(`/documents/${id}/retry`)
    return response.data
  },
}
export default documentService
