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
  isGuest: boolean
  authModalMessage: string | null
  login: (token: string, refreshToken: string, user: User) => void
  logout: () => Promise<void>
  requireAuth: (callback: () => void, message: string) => void
  closeAuthModal: () => void
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
  const [isGuest, setIsGuest] = useState(true)
  const [isLoading, setIsLoading] = useState(true)
  const [authModalMessage, setAuthModalMessage] = useState<string | null>(null)

  useEffect(() => {
    const savedToken = localStorage.getItem('accessToken')
    const savedUser = localStorage.getItem('currentUser')
    if (savedToken && savedUser && savedToken !== 'mock-token') {
      setToken(savedToken)
      setUser(JSON.parse(savedUser))
      setIsGuest(false)
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
      setToken('mock-token')
      setUser({
        id: 1,
        username: 'developer',
        email: 'dev@lumora.ai',
        roles: ['ROLE_USER']
      })
      setIsGuest(true)
    }
    setIsLoading(false)
  }, [])

  const login = (accessToken: string, refreshToken: string, userDetails: User) => {
    localStorage.setItem('accessToken', accessToken)
    localStorage.setItem('refreshToken', refreshToken)
    localStorage.setItem('currentUser', JSON.stringify(userDetails))
    
    setToken(accessToken)
    setUser(userDetails)
    setIsGuest(false)
    
    apiClient.defaults.headers.common['Authorization'] = `Bearer ${accessToken}`
  }

  const logout = async () => {
    try {
      await apiClient.post('/auth/logout')
    } catch (err) {
      console.error('Logout error on backend:', err)
    } finally {
      localStorage.setItem('accessToken', 'mock-token')
      localStorage.setItem('currentUser', JSON.stringify({
        id: 1,
        username: 'developer',
        email: 'dev@lumora.ai',
        roles: ['ROLE_USER']
      }))
      
      setToken('mock-token')
      setUser({
        id: 1,
        username: 'developer',
        email: 'dev@lumora.ai',
        roles: ['ROLE_USER']
      })
      setIsGuest(true)
      
      apiClient.defaults.headers.common['Authorization'] = `Bearer mock-token`
    }
  }

  const requireAuth = (callback: () => void, message: string) => {
    if (!isGuest) {
      callback()
    } else {
      setAuthModalMessage(message)
    }
  }

  const closeAuthModal = () => {
    setAuthModalMessage(null)
  }

  return (
    <AuthContext.Provider
      value={{
        user,
        token: token as string,
        isAuthenticated: !isGuest && !!token,
        isLoading,
        isGuest,
        authModalMessage,
        login,
        logout,
        requireAuth,
        closeAuthModal
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
