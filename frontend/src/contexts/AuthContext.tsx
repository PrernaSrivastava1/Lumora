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
  const [user, setUser] = useState<User | null>(null)
  const [token, setToken] = useState<String | null>(null)
  const [isLoading, setIsLoading] = useState(true)

  useEffect(() => {
    // Attempt Auto-Login by retrieving saved tokens
    const savedToken = localStorage.getItem('accessToken')
    const savedUser = localStorage.getItem('currentUser')
    if (savedToken && savedUser) {
      setToken(savedToken)
      setUser(JSON.parse(savedUser))
      
      // Configure initial authorization header
      apiClient.defaults.headers.common['Authorization'] = `Bearer ${savedToken}`
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
