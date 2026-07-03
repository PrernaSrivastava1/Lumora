import { createBrowserRouter } from 'react-router-dom'
import PageLayout from '@/components/layout/PageLayout'
import Dashboard from '@/pages/Dashboard'
import Workspaces from '@/pages/Workspaces'
import Documents from '@/pages/Documents'
import DocumentDetails from '@/pages/DocumentDetails'
import Search from '@/pages/Search'
import AIChat from '@/pages/AIChat'
import Analytics from '@/pages/Analytics'
import Benchmark from '@/pages/Benchmark'
import Settings from '@/pages/Settings'
import NotFound from '@/pages/NotFound'
import ErrorPage from '@/components/common/ErrorPage'
import Login from '@/pages/Login'
import Register from '@/pages/Register'
import { ProtectedRoute } from '@/components/layout/ProtectedRoute'
import Profile from '@/pages/Profile'

export const router = createBrowserRouter([
  {
    path: '/login',
    element: <Login />,
    errorElement: <ErrorPage />,
  },
  {
    path: '/register',
    element: <Register />,
    errorElement: <ErrorPage />,
  },
  {
    path: '/',
    element: <ProtectedRoute />,
    errorElement: <ErrorPage />,
    children: [
      {
        path: '',
        element: <PageLayout />,
        children: [
          {
            path: '',
            element: <Dashboard />,
          },
          {
            path: 'workspaces',
            element: <Workspaces />,
          },
          {
            path: 'documents',
            element: <Documents />,
          },
          {
            path: 'documents/:id',
            element: <DocumentDetails />,
          },
          {
            path: 'search',
            element: <Search />,
          },
          {
            path: 'chat',
            element: <AIChat />,
          },
          {
            path: 'analytics',
            element: <Analytics />,
          },
          {
            path: 'benchmark',
            element: <Benchmark />,
          },
          {
            path: 'settings',
            element: <Settings />,
          },
          {
            path: 'profile',
            element: <Profile />,
          },
          {
            path: '*',
            element: <NotFound />,
          },
        ],
      },
    ],
  },
])
