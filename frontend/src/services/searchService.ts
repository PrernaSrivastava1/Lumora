import apiClient from './api'
import type { ApiResponse, SearchRequest, SearchResponse } from '@/types'

export const searchService = {
  search: async (data: SearchRequest): Promise<ApiResponse<SearchResponse>> => {
    const response = await apiClient.post<ApiResponse<SearchResponse>>('/search', data)
    return response.data
  },
}

export default searchService
