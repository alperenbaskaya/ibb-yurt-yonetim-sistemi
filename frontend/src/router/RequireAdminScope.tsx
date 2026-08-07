import { Navigate, Outlet } from 'react-router-dom'
import { useAuth } from '../features/auth/useAuth'
import type { AdminScope } from '../types/auth'

interface RequireAdminScopeProps {
  scope: AdminScope
}

export function RequireAdminScope({ scope }: RequireAdminScopeProps) {
  const { user } = useAuth()

  if (user?.role !== 'ADMIN' || user.adminScope !== scope) {
    return <Navigate to="/forbidden" replace />
  }

  return <Outlet />
}
