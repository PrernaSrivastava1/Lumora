import axios from 'axios'

// Configured base instance for Spring Boot REST API communications.
// Proxied via vite server settings or direct connection configurations.
const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 60000,
  headers: {
    'Content-Type': 'application/json',
    Accept: 'application/json',
  },
})

export default apiClient
