import { Link, useLocation } from 'react-router-dom'
import { ChevronRight, Home } from 'lucide-react'

export default function Breadcrumbs() {
  const { pathname } = useLocation()
  const pathnames = pathname.split('/').filter((x) => x)

  const formatSegment = (str: string) => {
    if (str === 'chat') return 'AI Chat'
    return str.charAt(0).toUpperCase() + str.slice(1).replace('-', ' ')
  }

  return (
    <nav className="flex items-center gap-1.5 text-xs text-muted-foreground py-2 px-6 bg-card border-b border-border">
      <Link to="/" className="flex items-center gap-1 hover:text-foreground transition-colors">
        <Home className="h-3 w-3" />
        <span>Dashboard</span>
      </Link>
      {pathnames.map((value, index) => {
        const to = `/${pathnames.slice(0, index + 1).join('/')}`
        const isLast = index === pathnames.length - 1

        return (
          <div key={to} className="flex items-center gap-1.5">
            <ChevronRight className="h-3.5 w-3.5 text-muted-foreground/60" />
            {isLast ? (
              <span className="font-semibold text-foreground">{formatSegment(value)}</span>
            ) : (
              <Link to={to} className="hover:text-foreground transition-colors">
                {formatSegment(value)}
              </Link>
            )}
          </div>
        )
      })}
    </nav>
  )
}
