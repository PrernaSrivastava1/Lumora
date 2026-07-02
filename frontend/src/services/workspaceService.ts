import apiClient from './api'
import type { ApiResponse, Workspace } from '@/types'

export const workspaceService = {
  getWorkspaces: async (): Promise<ApiResponse<Workspace[]>> => {
    const response = await apiClient.get<ApiResponse<Workspace[]>>('/workspaces')
    return response.data
  },

  getWorkspaceById: async (id: number): Promise<ApiResponse<Workspace>> => {
    const response = await apiClient.get<ApiResponse<Workspace>>(`/workspaces/${id}`)
    return response.data
  },

  createWorkspace: async (data: { name: string; description: string }): Promise<ApiResponse<Workspace>> => {
    const response = await apiClient.post<ApiResponse<Workspace>>('/workspaces', data)
    return response.data
  },

  updateWorkspace: async (id: number, data: { name: string; description: string }): Promise<ApiResponse<Workspace>> => {
    const response = await apiClient.put<ApiResponse<Workspace>>(`/workspaces/${id}`, data)
    return response.data
  },

  deleteWorkspace: async (id: number): Promise<ApiResponse<void>> => {
    const response = await apiClient.delete<ApiResponse<void>>(`/workspaces/${id}`)
    return response.data
  },
}
export default workspaceService
