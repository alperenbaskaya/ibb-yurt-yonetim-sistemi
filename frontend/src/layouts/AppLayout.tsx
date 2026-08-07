import { useEffect, useState } from 'react'
import { Outlet, useLocation } from 'react-router-dom'
import { Sidebar } from '../components/layout/Sidebar'
import { Topbar } from '../components/layout/Topbar'
import { useAuth } from '../features/auth/useAuth'
import { UserConfigurationErrorPage } from '../pages/UserConfigurationErrorPage'
import { getUserCategory } from '../utils/userCategory'

export function AppLayout() {
  const location = useLocation()
  const { user } = useAuth()
  const [isSidebarOpen, setIsSidebarOpen] = useState(false)

  useEffect(() => {
    setIsSidebarOpen(false)
  }, [location.pathname])

  if (!user) {
    return null
  }

  const category = getUserCategory(user)

  if (!category) {
    return <UserConfigurationErrorPage />
  }

  return (
    <div className="min-h-screen bg-slate-50">
      <Sidebar
        category={category}
        isOpen={isSidebarOpen}
        onClose={() => setIsSidebarOpen(false)}
      />

      <div className="min-h-screen lg:pl-72">
        <Topbar
          category={category}
          onOpenSidebar={() => setIsSidebarOpen(true)}
        />
        <main className="px-4 py-6 sm:px-6 sm:py-8 lg:px-8 lg:py-9">
          <div className="mx-auto max-w-7xl">
            <Outlet />
          </div>
        </main>
      </div>
    </div>
  )
}
