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
