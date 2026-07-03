export interface Workspace {
  id: number
  name: string
  description: string
  createdAt: string
  updatedAt: string
  totalDocuments: number
  totalVectors: number
}

export interface ApiResponse<T> {
  success: boolean
  message: string
  data: T
}

export interface Document {
  id: number
  workspaceId: number
  title: string
  originalFileName: string
  fileType: string
  size: number
  uploadTime: string
  processingStatus: 'READY' | 'UPLOADING' | 'PROCESSING' | 'FAILED'
  totalChunks: number
}

export interface SearchResult {
  documentId?: number
  chunkId: number
  score: number
  matchedText?: string
  explanation: string
}

export interface SearchResponse {
  query: string
  algorithm: string
  metric: string
  executionTime: number
  resultCount: number
  results: SearchResult[]
}

export interface SearchRequest {
  query: string
  algorithm: string
  metric: string
  topK: number
  workspaceId: number
}

