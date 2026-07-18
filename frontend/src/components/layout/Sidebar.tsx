import { NavLink } from 'react-router-dom'
import { useLayout } from '@/contexts/LayoutContext'
import {
  LayoutDashboard,
  FolderOpen,
  FileText,
  Search,
  Orbit,
  MessageSquare,
  BarChart3,
  Flame,
  Settings,
  ChevronLeft,
  ChevronRight,
  X,
} from 'lucide-react'

const navigation = [
  { name: 'Home', description: 'Pick up where you left off', to: '/home', icon: LayoutDashboard },
  { name: 'Workspaces', description: 'Organize knowledge by project', to: '/workspaces', icon: FolderOpen },
  { name: 'Documents', description: 'Manage indexed source material', to: '/documents', icon: FileText },
  { name: 'Search', description: 'Find meaning across documents', to: '/search', icon: Search },
  { name: 'AI Assistant', description: 'Ask questions with citations', to: '/chat', icon: MessageSquare },
  { name: 'Knowledge map', description: 'Explore semantic relationships', to: '/knowledge-map', icon: Orbit },
  { name: 'Analytics', description: 'Understand retrieval insights', to: '/analytics', icon: BarChart3 },
  { name: 'Benchmarks', description: 'Compare search performance', to: '/benchmark', icon: Flame },
  { name: 'Settings', description: 'Configure preferences and system', to: '/settings', icon: Settings },
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
        className={`fixed inset-y-0 left-0 z-50 md:sticky md:top-[4.5rem] md:z-30 flex flex-col h-[calc(100vh-4.5rem)] border-r border-border/80 bg-card/75 backdrop-blur-xl text-card-foreground transition-all duration-300 ${
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
        <div className="flex-1 py-5 px-3 space-y-1 overflow-y-auto">
          {navigation.map((item) => (
            <NavLink
              key={item.name}
              to={item.to}
              onClick={closeMobileSidebar}
              className={({ isActive }) =>
                `flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm font-medium transition-all group ${
                  isActive
                    ? 'bg-primary text-primary-foreground shadow-sm'
                    : 'text-muted-foreground hover:text-foreground hover:bg-secondary/75'
                }`
              }
              title={sidebarCollapsed ? item.name : undefined}
            >
              <item.icon className="h-4 w-4 shrink-0" />
              <span
                className={`min-w-0 transition-opacity duration-200 ${
                  sidebarCollapsed ? 'md:hidden opacity-0' : 'opacity-100'
                }`}
              >
                <span className="block leading-4">{item.name}</span>
                <span className={`mt-0.5 block truncate text-[10px] font-medium leading-3 ${sidebarCollapsed ? '' : 'opacity-70'} ${'group-[.active]:text-primary-foreground'}`}>{item.description}</span>
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
