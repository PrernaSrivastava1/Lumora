import { NavLink } from 'react-router-dom'
import { useLayout } from '@/contexts/LayoutContext'
import {
  LayoutDashboard,
  FolderOpen,
  FileText,
  Search,
  MessageSquare,
  BarChart3,
  Flame,
  Settings,
  ChevronLeft,
  ChevronRight,
  X,
} from 'lucide-react'

const navigation = [
  { name: 'Dashboard', to: '/', icon: LayoutDashboard },
  { name: 'Workspaces', to: '/workspaces', icon: FolderOpen },
  { name: 'Documents', to: '/documents', icon: FileText },
  { name: 'Semantic Search', to: '/search', icon: Search },
  { name: 'AI Assistant', to: '/chat', icon: MessageSquare },
  { name: 'Analytics', to: '/analytics', icon: BarChart3 },
  { name: 'Benchmark Center', to: '/benchmark', icon: Flame },
  { name: 'Settings', to: '/settings', icon: Settings },
]

export default function Sidebar() {
  const {
    sidebarCollapsed,
    toggleSidebar,
    mobileSidebarOpen,
    closeMobileSidebar,
  } = useLayout()

  return (
    <>
      {/* Mobile Drawer Overlay */}
      {mobileSidebarOpen && (
        <div
          className="fixed inset-0 z-50 bg-background/80 backdrop-blur-sm md:hidden"
          onClick={closeMobileSidebar}
        />
      )}

      {/* Sidebar Container */}
      <aside
        className={`fixed inset-y-0 left-0 z-50 md:sticky md:top-16 md:z-30 flex flex-col h-[calc(100vh-4rem)] border-r border-border bg-card text-card-foreground transition-all duration-300 ${
          mobileSidebarOpen
            ? 'translate-x-0 w-64'
            : '-translate-x-full md:translate-x-0'
        } ${sidebarCollapsed ? 'md:w-16' : 'md:w-64'}`}
      >
        {/* Mobile Header */}
        <div className="flex h-16 items-center justify-between px-4 md:hidden border-b border-border">
          <span className="font-bold text-sm">Navigation</span>
          <button
            onClick={closeMobileSidebar}
            className="rounded-md p-1 hover:bg-secondary text-muted-foreground hover:text-foreground"
          >
            <X className="h-5 w-5" />
          </button>
        </div>

        {/* Links List */}
        <div className="flex-1 py-6 px-3 space-y-1 overflow-y-auto">
          {navigation.map((item) => (
            <NavLink
              key={item.name}
              to={item.to}
              onClick={closeMobileSidebar}
              className={({ isActive }) =>
                `flex items-center gap-3 px-3 py-2 rounded-lg text-sm font-medium transition-all group ${
                  isActive
                    ? 'bg-secondary text-foreground shadow-sm'
                    : 'text-muted-foreground hover:text-foreground hover:bg-secondary/40'
                }`
              }
              title={sidebarCollapsed ? item.name : undefined}
            >
              <item.icon className="h-4 w-4 shrink-0" />
              <span
                className={`transition-opacity duration-200 ${
                  sidebarCollapsed ? 'md:hidden opacity-0' : 'opacity-100'
                }`}
              >
                {item.name}
              </span>
            </NavLink>
          ))}
        </div>

        {/* Desktop Collapse Controller */}
        <div className="hidden md:flex justify-end p-3 border-t border-border">
          <button
            onClick={toggleSidebar}
            className="rounded-md p-1.5 hover:bg-secondary text-muted-foreground hover:text-foreground transition-colors"
            title={sidebarCollapsed ? 'Expand Sidebar' : 'Collapse Sidebar'}
          >
            {sidebarCollapsed ? (
              <ChevronRight className="h-4 w-4" />
            ) : (
              <ChevronLeft className="h-4 w-4" />
            )}
          </button>
        </div>
      </aside>
    </>
  )
}
