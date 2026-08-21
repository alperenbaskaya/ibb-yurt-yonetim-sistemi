import { X } from 'lucide-react'
import { NavLink } from 'react-router-dom'
import type { UserCategory } from '../../types/userCategory'
import { navigationByCategory } from '../../router/navigation'
import { userCategoryLabels } from '../../utils/userCategory'
import { AppBrand } from '../common/AppBrand'

interface SidebarProps {
  category: UserCategory
  isOpen: boolean
  onClose: () => void
}

export function Sidebar({ category, isOpen, onClose }: SidebarProps) {
  const navigationItems = navigationByCategory[category]

  return (
    <>
      {isOpen && (
        <button
          type="button"
          className="fixed inset-0 z-40 bg-slate-950/35 lg:hidden"
          onClick={onClose}
          aria-label="Menüyü kapat"
        />
      )}

      <aside
        className={`fixed inset-y-0 left-0 z-50 flex w-72 flex-col border-r border-slate-200 bg-white transition-transform duration-200 lg:translate-x-0 ${
          isOpen ? 'translate-x-0' : '-translate-x-full'
        }`}
        aria-label="Ana menü"
      >
        <div className="flex h-20 shrink-0 items-center justify-between border-b border-slate-200 px-5">
          <NavLink
            to="/home"
            onClick={onClose}
            className="flex items-center gap-3 focus-visible:outline-2 focus-visible:outline-offset-4 focus-visible:outline-blue-700"
          >
            <AppBrand />
          </NavLink>

          <button
            type="button"
            onClick={onClose}
            className="flex size-9 items-center justify-center text-slate-500 hover:bg-slate-100 hover:text-slate-900 focus-visible:outline-2 focus-visible:outline-offset-1 focus-visible:outline-blue-700 lg:hidden"
            aria-label="Menüyü kapat"
          >
            <X aria-hidden="true" size={21} />
          </button>
        </div>

        <nav className="flex-1 overflow-y-auto px-3 py-5" aria-label="Uygulama navigasyonu">
          <ul className="space-y-1">
            {navigationItems.map((item) => {
              const Icon = item.icon

              return (
                <li key={item.path}>
                  <NavLink
                    to={item.path}
                    onClick={onClose}
                    className={({ isActive }) =>
                      `flex min-h-11 items-center gap-3 border-l-2 px-3 py-2.5 text-sm font-medium transition-colors focus-visible:outline-2 focus-visible:outline-offset-1 focus-visible:outline-blue-700 ${
                        isActive
                          ? 'border-blue-700 bg-blue-50 text-blue-800'
                          : 'border-transparent text-slate-600 hover:bg-slate-50 hover:text-slate-950'
                      }`
                    }
                  >
                    <Icon aria-hidden="true" size={19} strokeWidth={1.8} />
                    <span>{item.label}</span>
                  </NavLink>
                </li>
              )
            })}
          </ul>
        </nav>

        <div className="border-t border-slate-200 px-5 py-4">
          <p className="text-xs font-medium uppercase tracking-[0.12em] text-slate-400">
            Kullanıcı Yetkisi
          </p>
          <p className="mt-1 text-sm font-semibold text-slate-700">
            {userCategoryLabels[category]}
          </p>
        </div>
      </aside>
    </>
  )
}
