import React, { useEffect } from 'react'
import { useNavigate, useLocation } from 'react-router-dom'
import { useAuth } from '@/contexts/AuthContext'
import { X, Sparkles, LogIn, UserPlus, FolderPlus, FileUp, MessageSquare, Database, User } from 'lucide-react'

/* ─────────────────────────────────────────────────────────
   Contextual benefit copy keyed by feature keyword
   ───────────────────────────────────────────────────────── */
const contextMap: Record<string, { icon: React.ElementType; title: string; benefits: string[] }> = {
  workspace: {
    icon: FolderPlus,
    title: 'Save your workspaces',
    benefits: [
      'Create unlimited knowledge workspaces',
      'Persist and revisit indexed sources anytime',
      'Collaborate and share context with your team',
    ],
  },
  document: {
    icon: FileUp,
    title: 'Upload & index documents',
    benefits: [
      'Index PDF, DOCX, TXT and Markdown files',
      'Build a searchable private knowledge base',
      'Automatic chunking and vector embedding',
    ],
  },
  chat: {
    icon: MessageSquare,
    title: 'Start an AI conversation',
    benefits: [
      'Ask questions grounded in your own documents',
      'Get cited, verifiable answers from local AI',
      'Save and revisit conversation history',
    ],
  },
  profile: {
    icon: User,
    title: 'Manage your account',
    benefits: [
      'Personalise your profile and preferences',
      'Secure your account with a custom password',
      'Sync settings across sessions',
    ],
  },
  settings: {
    icon: Database,
    title: 'Access system settings',
    benefits: [
      'Configure model bindings and index parameters',
      'Manage data persistence and maintenance',
      'Full control over your local RAG stack',
    ],
  },
}

function detectContext(message: string) {
  const lower = message.toLowerCase()
  if (lower.includes('workspace')) return contextMap.workspace
  if (lower.includes('document') || lower.includes('upload') || lower.includes('file')) return contextMap.document
  if (lower.includes('chat') || lower.includes('message') || lower.includes('conversation')) return contextMap.chat
  if (lower.includes('profile') || lower.includes('password')) return contextMap.profile
  if (lower.includes('setting') || lower.includes('purge') || lower.includes('database')) return contextMap.settings
  // Default
  return {
    icon: Sparkles,
    title: 'Sign in to continue',
    benefits: [
      'Create and save your personal workspaces',
      'Upload and index private document libraries',
      'Chat with local AI grounded in your sources',
    ],
  }
}

export default function AuthModal() {
  const { authModalMessage, closeAuthModal } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()

  // Close on Escape key
  useEffect(() => {
    if (!authModalMessage) return
    const handleKey = (e: KeyboardEvent) => {
      if (e.key === 'Escape') closeAuthModal()
    }
    window.addEventListener('keydown', handleKey)
    return () => window.removeEventListener('keydown', handleKey)
  }, [authModalMessage, closeAuthModal])

  if (!authModalMessage) return null

  const ctx = detectContext(authModalMessage)
  const CtxIcon = ctx.icon

  const handleRedirect = (path: string) => {
    closeAuthModal()
    navigate(path, { state: { from: location.pathname } })
  }

  return (
    <div
      className="fixed inset-0 z-[100] flex items-center justify-center p-4"
      role="dialog"
      aria-modal="true"
      aria-labelledby="auth-modal-title"
    >
      {/* Backdrop */}
      <div
        className="absolute inset-0 bg-background/60 backdrop-blur-md"
        onClick={closeAuthModal}
        aria-hidden="true"
      />

      {/* Card */}
      <div className="relative z-10 w-full max-w-sm animate-in zoom-in-95 fade-in duration-200">
        {/* Gradient glow behind card */}
        <div className="absolute -inset-px rounded-2xl bg-gradient-to-br from-primary/30 via-transparent to-primary/10 blur-sm" />

        <div className="relative rounded-2xl border border-border/60 bg-card shadow-2xl overflow-hidden">

          {/* Header band with subtle gradient */}
          <div className="relative px-6 pt-8 pb-6 text-center bg-gradient-to-b from-primary/5 to-transparent border-b border-border/40">

            {/* Close button */}
            <button
              id="auth-modal-close"
              onClick={closeAuthModal}
              className="absolute right-4 top-4 rounded-lg p-1.5 text-muted-foreground hover:text-foreground hover:bg-secondary/80 transition-all"
              title="Dismiss"
            >
              <X className="h-4 w-4" />
            </button>

            {/* Brand + context icon */}
            <div className="flex items-center justify-center gap-2 mb-5">
              <span className="grid h-11 w-11 place-items-center rounded-2xl bg-primary text-primary-foreground shadow-lg shadow-primary/20">
                <Sparkles className="h-5 w-5" />
              </span>
              <span className="text-lg font-bold tracking-tight text-foreground">Lumora</span>
            </div>

            <h2
              id="auth-modal-title"
              className="text-xl font-semibold tracking-tight text-foreground"
            >
              {ctx.title}
            </h2>
            <p className="mt-2 text-sm text-muted-foreground leading-relaxed">
              {authModalMessage}
            </p>
          </div>

          {/* Benefits */}
          <div className="px-6 py-5 space-y-3">
            {ctx.benefits.map((benefit) => (
              <div key={benefit} className="flex items-start gap-3">
                <span className="mt-0.5 flex h-5 w-5 shrink-0 items-center justify-center rounded-full bg-primary/10">
                  <span className="h-1.5 w-1.5 rounded-full bg-primary" />
                </span>
                <span className="text-sm text-muted-foreground leading-5">{benefit}</span>
              </div>
            ))}
          </div>

          {/* Actions */}
          <div className="px-6 pb-7 space-y-2.5">
            {/* Feature icon hint */}
            <div className="flex items-center gap-2 mb-4 p-3 rounded-xl bg-primary/5 border border-primary/10">
              <CtxIcon className="h-4 w-4 text-primary shrink-0" />
              <span className="text-xs text-primary font-medium">Free account required for this feature</span>
            </div>

            <button
              id="auth-modal-signin"
              onClick={() => handleRedirect('/login')}
              className="w-full flex justify-center items-center gap-2 rounded-xl bg-primary px-4 py-3 text-sm font-semibold text-primary-foreground shadow-sm shadow-primary/20 hover:opacity-95 active:scale-[.99] transition-all"
            >
              <LogIn className="h-4 w-4" />
              Sign In
            </button>

            <button
              id="auth-modal-register"
              onClick={() => handleRedirect('/register')}
              className="w-full flex justify-center items-center gap-2 rounded-xl border border-border bg-card px-4 py-3 text-sm font-semibold hover:bg-secondary active:scale-[.99] transition-all"
            >
              <UserPlus className="h-4 w-4 text-muted-foreground" />
              Create Free Account
            </button>

            <button
              id="auth-modal-dismiss"
              onClick={closeAuthModal}
              className="w-full pt-1 text-center text-xs text-muted-foreground hover:text-foreground transition-colors font-medium"
            >
              Continue exploring without an account →
            </button>
          </div>
        </div>
      </div>
    </div>
  )
}
