import React, { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '@/contexts/AuthContext'
import { KeyRound, Mail, Loader2, AlertCircle } from 'lucide-react'
import apiClient from '@/services/api'

export default function Login() {
  const { login } = useAuth()
  const navigate = useNavigate()
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [errorMsg, setErrorMsg] = useState('')

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!username.trim() || !password.trim()) return

    setIsSubmitting(true)
    setErrorMsg('')
    try {
      const res = await apiClient.post('/auth/login', { username, password })
      if (res.data.success) {
        const { token, refreshToken, id, email, roles } = res.data.data
        login(token, refreshToken, { id, username, email, roles })
        navigate('/home')
      } else {
        setErrorMsg(res.data.message || 'Login failed')
      }
    } catch (err: any) {
      setErrorMsg(err.response?.data?.message || 'Invalid credentials or connection error')
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-background px-4 py-12 sm:px-6 lg:px-8">
      <div className="w-full max-w-md space-y-8 surface p-8 sm:p-9">
        <div className="text-center">
          <p className="eyebrow mb-3">Welcome to Lumora</p>
          <h2 className="text-3xl font-semibold tracking-[-.04em]">
            Continue your work
          </h2>
          <p className="mt-2 text-sm text-muted-foreground">
            Sign in to access your local vector workspaces
          </p>
        </div>

        {errorMsg && (
          <div className="rounded-lg border border-red-500/20 bg-red-500/5 p-4 flex items-start gap-2.5 text-sm text-red-400">
            <AlertCircle className="h-5 w-5 shrink-0 mt-0.5" />
            <span>{errorMsg}</span>
          </div>
        )}

        <form className="mt-8 space-y-6" onSubmit={handleSubmit}>
          <div className="space-y-4">
            <div>
              <label className="text-xs font-semibold text-muted-foreground flex items-center gap-1.5 mb-1.5">
                <Mail className="h-3.5 w-3.5" /> Username
              </label>
              <input
                type="text"
                required
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                placeholder="Enter username"
                className="w-full px-3.5 py-2.5 rounded-lg border border-input bg-muted/10 text-sm focus:outline-none focus:ring-1 focus:ring-primary focus:border-primary transition-all"
              />
            </div>

            <div>
              <label className="text-xs font-semibold text-muted-foreground flex items-center gap-1.5 mb-1.5">
                <KeyRound className="h-3.5 w-3.5" /> Password
              </label>
              <input
                type="password"
                required
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="Enter password"
                className="w-full px-3.5 py-2.5 rounded-lg border border-input bg-muted/10 text-sm focus:outline-none focus:ring-1 focus:ring-primary focus:border-primary transition-all"
              />
            </div>
          </div>

          <div>
            <button
              type="submit"
              disabled={isSubmitting}
              className="w-full flex justify-center items-center gap-2 rounded-xl bg-primary px-4 py-3 text-sm font-semibold text-primary-foreground shadow-sm hover:opacity-95 active:scale-[.99] transition-all disabled:opacity-50"
            >
              {isSubmitting ? (
                <Loader2 className="h-4 w-4 animate-spin" />
              ) : (
                'Sign In'
              )}
            </button>
          </div>
        </form>

        <div className="text-center text-sm text-muted-foreground pt-4 border-t border-border">
          Don't have an account?{' '}
          <Link to="/register" className="font-semibold text-primary hover:underline">
            Create Account
          </Link>
        </div>
      </div>
    </div>
  )
}
