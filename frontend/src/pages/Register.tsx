import React, { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { KeyRound, Mail, User, Loader2, AlertCircle } from 'lucide-react'
import apiClient from '@/services/api'

export default function Register() {
  const navigate = useNavigate()
  const [username, setUsername] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [errorMsg, setErrorMsg] = useState('')
  const [successMsg, setSuccessMsg] = useState('')

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!username.trim() || !email.trim() || !password.trim()) return

    setIsSubmitting(true)
    setErrorMsg('')
    setSuccessMsg('')
    try {
      const res = await apiClient.post('/auth/register', { username, email, password })
      if (res.data.success) {
        setSuccessMsg('Account registered successfully! Redirecting to login...')
        setTimeout(() => {
          navigate('/login')
        }, 1500)
      } else {
        setErrorMsg(res.data.message || 'Registration failed')
      }
    } catch (err: any) {
      setErrorMsg(err.response?.data?.message || 'Failed to create account. Email or username might be in use.')
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-background px-4 py-12 sm:px-6 lg:px-8">
      <div className="w-full max-w-md space-y-8 surface p-8 sm:p-9">
        <div className="text-center">
          <p className="eyebrow mb-3">Build your knowledge layer</p>
          <h2 className="text-3xl font-semibold tracking-[-.04em]">
            Create your account
          </h2>
          <p className="mt-2 text-sm text-muted-foreground">
            Get started with local vector search indexes
          </p>
        </div>

        {errorMsg && (
          <div className="rounded-lg border border-red-500/20 bg-red-500/5 p-4 flex items-start gap-2.5 text-sm text-red-400">
            <AlertCircle className="h-5 w-5 shrink-0 mt-0.5" />
            <span>{errorMsg}</span>
          </div>
        )}

        {successMsg && (
          <div className="rounded-lg border border-emerald-500/20 bg-emerald-500/5 p-4 flex items-start gap-2.5 text-sm text-emerald-400">
            <AlertCircle className="h-5 w-5 shrink-0 mt-0.5 text-emerald-400" />
            <span>{successMsg}</span>
          </div>
        )}

        <form className="mt-8 space-y-6" onSubmit={handleSubmit}>
          <div className="space-y-4">
            <div>
              <label className="text-xs font-semibold text-muted-foreground flex items-center gap-1.5 mb-1.5">
                <User className="h-3.5 w-3.5" /> Username
              </label>
              <input
                type="text"
                required
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                placeholder="Choose a username"
                className="w-full px-3.5 py-2.5 rounded-lg border border-input bg-muted/10 text-sm focus:outline-none focus:ring-1 focus:ring-primary focus:border-primary transition-all"
              />
            </div>

            <div>
              <label className="text-xs font-semibold text-muted-foreground flex items-center gap-1.5 mb-1.5">
                <Mail className="h-3.5 w-3.5" /> Email Address
              </label>
              <input
                type="email"
                required
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="you@example.com"
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
                placeholder="Create a strong password (min 6 chars)"
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
                'Sign Up'
              )}
            </button>
          </div>
        </form>

        <div className="text-center text-sm text-muted-foreground pt-4 border-t border-border">
          Already have an account?{' '}
          <Link to="/login" className="font-semibold text-primary hover:underline">
            Sign In
          </Link>
        </div>
      </div>
    </div>
  )
}
