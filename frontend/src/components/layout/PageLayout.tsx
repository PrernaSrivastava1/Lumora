import { Outlet, useLocation } from 'react-router-dom'
import { motion, AnimatePresence } from 'framer-motion'
import { LayoutProvider } from '@/contexts/LayoutContext'
import Navbar from './Navbar'
import Sidebar from './Sidebar'
import Footer from './Footer'
import Breadcrumbs from '../common/Breadcrumbs'

export default function PageLayout() {
  const { pathname } = useLocation()

  return (
    <LayoutProvider>
      <div className="flex flex-col min-h-screen bg-background text-foreground transition-colors duration-200">
        <Navbar />
        <div className="flex flex-1 relative">
          <Sidebar />
          <div className="flex-1 flex flex-col min-w-0">
            <Breadcrumbs />
            <main className="flex-1 overflow-y-auto bg-muted/10">
              <AnimatePresence mode="wait">
                <motion.div
                  key={pathname}
                  initial={{ opacity: 0, y: 10 }}
                  animate={{ opacity: 1, y: 0 }}
                  exit={{ opacity: 0, y: -10 }}
                  transition={{ duration: 0.15 }}
                  className="h-full"
                >
                  <Outlet />
                </motion.div>
              </AnimatePresence>
            </main>
          </div>
        </div>
        <Footer />
      </div>
    </LayoutProvider>
  )
}
