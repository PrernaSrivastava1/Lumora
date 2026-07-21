import React from 'react'
import { useNavigate, useLocation } from 'react-router-dom'
import { useAuth } from '@/contexts/AuthContext'
import { X, Sparkles, LogIn, UserPlus } from 'lucide-react'

export default function AuthModal() {
  const { authModalMessage, closeAuthModal } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()

  if (!authModalMessage) return null

  const handleRedirect = (path: string) => {
    closeAuthModal()
    navigate(path, { state: { from: location.pathname } })
  }

  return (
    <div className="fixed inset-0 z-[100] flex items-center justify-center bg-black/60 backdrop-blur-sm p-4 animate-in fade-in duration-200">
      <div className="surface max-w-md w-full p-6 sm:p-8 space-y-6 bg-card border border-border shadow-2xl relative animate-in zoom-in-95 duration-200">
        
        {/* Close Button */}
        <button
          onClick={closeAuthModal}
          className="absolute right-4 top-4 p-1.5 rounded-lg text-muted-foreground hover:text-foreground hover:bg-secondary transition-all"
          title="Dismiss"
        >
          <X className="h-4.5 w-4.5" />
        </button>

        {/* Brand Icon and Header */}
        <div className="text-center space-y-3">
          <span className="mx-auto grid h-12 w-12 place-items-center rounded-2xl bg-primary/10 text-primary shadow-sm">
            <Sparkles className="h-6 w-6" />
          </span>
          <div className="space-y-1">
            <h2 className="text-xl font-bold tracking-tight text-foreground">
              Sign in to Lumora
            </h2>
            <p className="text-xs text-muted-foreground">
              {authModalMessage}
            </p>
          </div>
        </div>

        {/* Feature Benefits List */}
        <div className="bg-muted/10 border border-border rounded-xl p-4 text-xs space-y-2.5 text-muted-foreground">
          <div className="flex gap-2">
            <span className="text-primary font-bold">✓</span>
            <span>Create and configure unlimited vector workspaces</span>
          </div>
          <div className="flex gap-2">
            <span className="text-primary font-bold">✓</span>
            <span>Index your personal PDF, DOCX, TXT, and Markdown files</span>
          </div>
          <div className="flex gap-2">
            <span className="text-primary font-bold">✓</span>
            <span>Save context-grounded conversations with local AI models</span>
          </div>
        </div>

        {/* Action Buttons */}
        <div className="space-y-2 pt-2">
          <button
            onClick={() => handleRedirect('/login')}
            className="w-full flex justify-center items-center gap-2 rounded-xl bg-primary px-4 py-3 text-sm font-semibold text-primary-foreground shadow-sm hover:opacity-95 active:scale-[.99] transition-all"
          >
            <LogIn className="h-4.5 w-4.5" />
            Sign In
          </button>
          
          <button
            onClick={() => handleRedirect('/register')}
            className="w-full flex justify-center items-center gap-2 rounded-xl border border-border bg-card px-4 py-3 text-sm font-semibold hover:bg-secondary active:scale-[.99] transition-all"
          >
            <UserPlus className="h-4.5 w-4.5 text-muted-foreground" />
            Create Free Account
          </button>

          <button
            onClick={closeAuthModal}
            className="w-full text-center text-xs text-muted-foreground hover:text-foreground hover:underline transition-all pt-2 block font-medium"
          >
            Continue Exploring as Guest
          </button>
        </div>

      </div>
    </div>
  )
}
