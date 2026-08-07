import { Navigate, Outlet } from 'react-router-dom'
import { useAuth } from '../features/auth/useAuth'
import type { Role } from '../types/auth'

interface RequireRoleProps {
  role: Role
}

export function RequireRole({ role }: RequireRoleProps) {
  const { user } = useAuth()

  if (!user || user.role !== role) {
    return <Navigate to="/forbidden" replace />
  }

  return <Outlet />
}
