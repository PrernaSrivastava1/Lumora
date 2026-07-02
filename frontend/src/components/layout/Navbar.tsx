import ThemeToggle from '@/components/common/ThemeToggle'
import { useLayout } from '@/contexts/LayoutContext'
import { Database, Menu, Bell, Search, User } from 'lucide-react'
import { Link } from 'react-router-dom'

export default function Navbar() {
  const { toggleMobileSidebar } = useLayout()

  return (
    <header className="sticky top-0 z-40 w-full border-b border-border bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60">
      <div className="flex h-16 items-center justify-between px-6">
        <div className="flex items-center gap-3">
          <button
            onClick={toggleMobileSidebar}
            className="md:hidden rounded-md p-1.5 hover:bg-secondary text-muted-foreground hover:text-foreground transition-colors"
            title="Toggle Menu"
          >
            <Menu className="h-5 w-5" />
          </button>
          <Link to="/" className="flex items-center gap-2">
            <Database className="h-6 w-6 text-primary" />
            <span className="font-bold text-lg tracking-tight hidden sm:inline">SemanticVault AI</span>
          </Link>
        </div>

        {/* Global Search Placeholder */}
        <div className="hidden md:flex items-center flex-1 max-w-md mx-8 relative">
          <Search className="absolute left-3 h-4 w-4 text-muted-foreground" />
          <input
            type="text"
            placeholder="Search index resources, documents, logs..."
            disabled
            className="w-full pl-9 pr-4 py-1.5 rounded-lg border border-input bg-muted/40 text-sm placeholder-muted-foreground focus:outline-none cursor-not-allowed"
          />
        </div>

        <div className="flex items-center gap-4">
          <ThemeToggle />

          {/* Notifications Placeholder */}
          <button
            className="relative rounded-full p-1.5 hover:bg-secondary text-muted-foreground hover:text-foreground transition-colors cursor-not-allowed"
            disabled
            title="Notifications"
          >
            <Bell className="h-4 w-4" />
            <span className="absolute top-1.5 right-1.5 h-2 w-2 rounded-full bg-destructive" />
          </button>

          {/* User Profile Placeholder */}
          <div className="h-8 w-8 rounded-full border border-border flex items-center justify-center bg-muted cursor-not-allowed">
            <User className="h-4 w-4 text-muted-foreground" />
          </div>
        </div>
      </div>
    </header>
  )
}
