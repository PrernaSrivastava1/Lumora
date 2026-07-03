import React, { createContext, useContext, useState, useEffect } from 'react'
import apiClient from '@/services/api'

interface User {
  id: number
  username: string
  email: string
  roles: string[]
}

interface AuthContextType {
  user: User | null
  token: string | null
  isAuthenticated: boolean
  isLoading: boolean
  login: (token: string, refreshToken: string, user: User) => void
  logout: () => Promise<void>
}

const AuthContext = createContext<AuthContextType | undefined>(undefined)

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>({
    id: 1,
    username: 'developer',
    email: 'dev@lumora.ai',
    roles: ['ROLE_USER']
  })
  const [token, setToken] = useState<string | null>('mock-token')
  const [isLoading, setIsLoading] = useState(true)

  useEffect(() => {
    const savedToken = localStorage.getItem('accessToken')
    const savedUser = localStorage.getItem('currentUser')
    if (savedToken && savedUser) {
      setToken(savedToken)
      setUser(JSON.parse(savedUser))
      apiClient.defaults.headers.common['Authorization'] = `Bearer ${savedToken}`
    } else {
      // Fallback default token for credentials-free exploration
      localStorage.setItem('accessToken', 'mock-token')
      localStorage.setItem('currentUser', JSON.stringify({
        id: 1,
        username: 'developer',
        email: 'dev@lumora.ai',
        roles: ['ROLE_USER']
      }))
    }
    setIsLoading(false)
  }, [])

  const login = (accessToken: string, refreshToken: string, userDetails: User) => {
    localStorage.setItem('accessToken', accessToken)
    localStorage.setItem('refreshToken', refreshToken)
    localStorage.setItem('currentUser', JSON.stringify(userDetails))
    
    setToken(accessToken)
    setUser(userDetails)
    
    apiClient.defaults.headers.common['Authorization'] = `Bearer ${accessToken}`
  }

  const logout = async () => {
    try {
      await apiClient.post('/auth/logout')
    } catch (err) {
      console.error('Logout error on backend:', err)
    } finally {
      localStorage.removeItem('accessToken')
      localStorage.removeItem('refreshToken')
      localStorage.removeItem('currentUser')
      
      setToken(null)
      setUser(null)
      
      delete apiClient.defaults.headers.common['Authorization']
    }
  }

  return (
    <AuthContext.Provider
      value={{
        user,
        token: token as string,
        isAuthenticated: !!token,
        isLoading,
        login,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  )
}

export const useAuth = () => {
  const context = useContext(AuthContext)
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider')
  }
  return context
}
