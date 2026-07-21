import ThemeToggle from '@/components/common/ThemeToggle'
import { useLayout } from '@/contexts/LayoutContext'
import { Sparkles, Menu, Bell, Search, User } from 'lucide-react'
import { Link } from 'react-router-dom'

export default function Navbar() {
  const { toggleMobileSidebar } = useLayout()

  return (
    <header className="sticky top-0 z-40 w-full border-b border-border/80 bg-background/85 backdrop-blur-xl">
      <div className="flex h-[4.5rem] items-center justify-between px-4 md:px-7">
        <div className="flex items-center gap-3">
          <button
            onClick={toggleMobileSidebar}
            className="md:hidden rounded-md p-1.5 hover:bg-secondary text-muted-foreground hover:text-foreground transition-colors"
            title="Toggle Menu"
          >
            <Menu className="h-5 w-5" />
          </button>
          <Link to="/home" className="flex items-center gap-2.5">
            <span className="grid h-8 w-8 place-items-center rounded-xl bg-primary text-primary-foreground shadow-sm"><Sparkles className="h-4 w-4" /></span>
            <span className="font-semibold text-[1.05rem] tracking-[-.03em] hidden sm:inline">Lumora</span>
          </Link>
        </div>

        {/* Global Search Placeholder */}
        <div className="hidden md:flex items-center flex-1 max-w-lg mx-10 relative">
          <Search className="absolute left-3 h-4 w-4 text-muted-foreground" />
          <input
            type="text"
            placeholder="Search your workspace"
            disabled
            className="w-full pl-9 pr-4 py-2 rounded-xl border border-input/80 bg-card/70 text-sm placeholder-muted-foreground focus:outline-none cursor-not-allowed"
          />
        </div>

        <div className="flex items-center gap-2">
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

          {/* User Profile Link */}
          <Link
            to="/profile"
            className="h-8 w-8 rounded-full border border-border flex items-center justify-center bg-secondary hover:bg-muted text-muted-foreground hover:text-foreground transition-colors"
            title="Profile & Settings"
          >
            <User className="h-4 w-4" />
          </Link>
        </div>
      </div>
    </header>
  )
}
