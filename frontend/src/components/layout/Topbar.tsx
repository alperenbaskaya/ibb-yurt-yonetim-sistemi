import { Bell, ChevronDown, LogOut, Menu, UserRound } from 'lucide-react'
import { Link, useLocation } from 'react-router-dom'
import { useAuth } from '../../features/auth/useAuth'
import { useMyUnreadCount } from '../../features/notifications/notificationQueries'
import type { UserCategory } from '../../types/userCategory'
import { getPageTitle } from '../../router/navigation'
import { userCategoryLabels } from '../../utils/userCategory'
import { ThemeToggle } from '../common/ThemeToggle'

interface TopbarProps {
  category: UserCategory
  onOpenSidebar: () => void
}

export function Topbar({ category, onOpenSidebar }: TopbarProps) {
  const location = useLocation()
  const { user, logout } = useAuth()
  const unreadCountQuery = useMyUnreadCount(user?.userId ?? null)

  if (!user) {
    return null
  }

  const initials = `${user.firstName.charAt(0)}${user.lastName.charAt(0)}`
  const unreadCount = unreadCountQuery.data?.unreadCount ?? 0
  const notificationLabel =
    unreadCount > 0 ? `Bildirimler, ${unreadCount} okunmamış` : 'Bildirimler'

  return (
    <header className="sticky top-0 z-30 flex h-20 items-center border-b border-slate-200 bg-white px-4 sm:px-6 lg:px-8">
      <button
        type="button"
        onClick={onOpenSidebar}
        className="mr-3 flex size-10 items-center justify-center text-slate-600 hover:bg-slate-100 hover:text-slate-950 focus-visible:outline-2 focus-visible:outline-offset-1 focus-visible:outline-blue-700 lg:hidden"
        aria-label="Menüyü aç"
      >
        <Menu aria-hidden="true" size={22} />
      </button>

      <div className="min-w-0 flex-1">
        <p className="truncate text-sm text-slate-500">Yurt Belge Yönetim Sistemi</p>
        <p className="truncate text-base font-semibold text-slate-950">
          {getPageTitle(location.pathname)}
        </p>
      </div>

      <div className="ml-3 flex items-center gap-2 sm:gap-3">
        <ThemeToggle />

        <Link
          to="/notifications"
          className="relative flex size-10 items-center justify-center text-slate-600 hover:bg-slate-100 hover:text-blue-800 focus-visible:outline-2 focus-visible:outline-offset-1 focus-visible:outline-blue-700"
          aria-label={notificationLabel}
        >
          <Bell aria-hidden="true" size={20} />
          {unreadCount > 0 && (
            <span className="absolute right-0.5 top-0.5 flex min-w-4.5 items-center justify-center rounded-full bg-red-600 px-1 text-[10px] font-bold leading-[18px] text-white ring-2 ring-white">
              {unreadCount > 99 ? '99+' : unreadCount}
            </span>
          )}
        </Link>

        <div className="hidden h-8 w-px bg-slate-200 sm:block" />

        <details className="group relative">
          <summary className="flex cursor-pointer list-none items-center gap-3 px-1 py-1 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-blue-700">
            <span className="flex size-10 items-center justify-center bg-blue-100 text-sm font-bold text-blue-800">
              {initials.toLocaleUpperCase('tr-TR')}
            </span>
            <span className="hidden max-w-44 text-left sm:block">
              <span className="block truncate text-sm font-semibold text-slate-900">
                {user.firstName} {user.lastName}
              </span>
              <span className="block truncate text-xs text-slate-500">
                {userCategoryLabels[category]}
              </span>
            </span>
            <ChevronDown
              aria-hidden="true"
              className="hidden text-slate-400 transition-transform group-open:rotate-180 sm:block"
              size={16}
            />
          </summary>

          <div className="absolute right-0 mt-2 w-56 border border-slate-200 bg-white p-2 shadow-lg">
            <div className="border-b border-slate-100 px-3 py-2 sm:hidden">
              <p className="truncate text-sm font-semibold text-slate-900">
                {user.firstName} {user.lastName}
              </p>
              <p className="truncate text-xs text-slate-500">
                {userCategoryLabels[category]}
              </p>
            </div>
            <Link
              to="/profile"
              className="flex items-center gap-2 px-3 py-2.5 text-sm font-medium text-slate-700 hover:bg-slate-50 hover:text-slate-950 focus-visible:outline-2 focus-visible:outline-blue-700"
            >
              <UserRound aria-hidden="true" size={17} />
              Profil
            </Link>
            <button
              type="button"
              onClick={logout}
              className="flex w-full items-center gap-2 px-3 py-2.5 text-left text-sm font-medium text-red-700 hover:bg-red-50 focus-visible:outline-2 focus-visible:outline-red-700"
            >
              <LogOut aria-hidden="true" size={17} />
              Çıkış Yap
            </button>
          </div>
        </details>
      </div>
    </header>
  )
}
