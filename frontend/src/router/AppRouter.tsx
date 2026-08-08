import { lazy, Suspense, type ReactNode } from 'react'
import { Navigate, Route, Routes } from 'react-router-dom'
import { RouteLoadingFallback } from '../components/common/RouteLoadingFallback'
import { LoginPage } from '../features/auth/LoginPage'
import { AppLayout } from '../layouts/AppLayout'
import { ForbiddenPage } from '../pages/ForbiddenPage'
import { HomePage } from '../pages/HomePage'
import { ProfilePage } from '../pages/ProfilePage'
import { PublicOnlyRoute } from './PublicOnlyRoute'
import { RequireAdminScope } from './RequireAdminScope'
import { RequireAuth } from './RequireAuth'
import { RequireRole } from './RequireRole'

const NotificationPage = lazy(() =>
  import('../features/notifications/NotificationPage').then((module) => ({
    default: module.NotificationPage,
  })),
)
const StudentRoutes = lazy(() => import('../features/student/StudentRoutes'))
const ReviewerRoutes = lazy(() => import('../features/reviewer/ReviewerRoutes'))
const DormitoryAdminRoutes = lazy(() => import('../features/dormitory-admin/DormitoryAdminRoutes'))
const GlobalAdminRoutes = lazy(() => import('../features/global-admin/GlobalAdminRoutes'))

function LazyRoute({ children }: { children: ReactNode }) {
  return <Suspense fallback={<RouteLoadingFallback />}>{children}</Suspense>
}

export function AppRouter() {
  return (
    <Routes>
      <Route element={<PublicOnlyRoute />}>
        <Route path="/login" element={<LoginPage />} />
      </Route>

      <Route element={<RequireAuth />}>
        <Route element={<AppLayout />}>
          <Route path="/home" element={<HomePage />} />
          <Route path="/profile" element={<ProfilePage />} />
          <Route path="/notifications" element={<LazyRoute><NotificationPage /></LazyRoute>} />
          <Route path="/forbidden" element={<ForbiddenPage />} />

          <Route element={<RequireRole role="STUDENT" />}>
            <Route path="/student/*" element={<LazyRoute><StudentRoutes /></LazyRoute>} />
          </Route>

          <Route element={<RequireRole role="REVIEWER" />}>
            <Route path="/reviewer/*" element={<LazyRoute><ReviewerRoutes /></LazyRoute>} />
          </Route>

          <Route element={<RequireRole role="ADMIN" />}>
            <Route element={<RequireAdminScope scope="DORMITORY" />}>
              <Route path="/admin/*" element={<LazyRoute><DormitoryAdminRoutes /></LazyRoute>} />
            </Route>

            <Route element={<RequireAdminScope scope="GLOBAL" />}>
              <Route path="/global/*" element={<LazyRoute><GlobalAdminRoutes /></LazyRoute>} />
            </Route>
          </Route>

          <Route path="/" element={<Navigate to="/home" replace />} />
          <Route path="*" element={<Navigate to="/home" replace />} />
        </Route>
      </Route>
    </Routes>
  )
}
