import axios from 'axios'

// Configured base instance for Spring Boot REST API communications.
// Proxied via vite server settings or direct connection configurations.
const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api/v1',
  timeout: 60000,
  headers: {
    'Content-Type': 'application/json',
    Accept: 'application/json',
  },
})

// Attach access token automatically if present
apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('accessToken')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// Intercept 401 responses to auto-refresh access tokens
apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true
      const refreshToken = localStorage.getItem('refreshToken')
      if (refreshToken) {
        try {
          // Attempt token exchange
          const refreshRes = await axios.post('/api/v1/auth/refresh', { refreshToken })
          if (refreshRes.data.success) {
            const newAccessToken = refreshRes.data.data.accessToken
            localStorage.setItem('accessToken', newAccessToken)
            apiClient.defaults.headers.common['Authorization'] = `Bearer ${newAccessToken}`
            originalRequest.headers.Authorization = `Bearer ${newAccessToken}`
            return apiClient(originalRequest)
          }
        } catch (refreshError) {
          // Refresh failed, clear session
          localStorage.removeItem('accessToken')
          localStorage.removeItem('refreshToken')
          localStorage.removeItem('currentUser')
          window.location.href = '/login'
        }
      }
    }
    return Promise.reject(error)
  }
)

export default apiClient
