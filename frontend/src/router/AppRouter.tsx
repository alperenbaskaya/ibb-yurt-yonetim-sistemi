import { Navigate, Route, Routes } from 'react-router-dom'
import { LoginPage } from '../features/auth/LoginPage'
import { DormitoryAdminDashboardPage } from '../features/dormitory-admin/DormitoryAdminDashboardPage'
import { DormitoryAdmissionsPage } from '../features/dormitory-admin/DormitoryAdmissionsPage'
import { DormitoryDocumentsPage } from '../features/dormitory-admin/DormitoryDocumentsPage'
import { DormitoryReviewersPage } from '../features/dormitory-admin/DormitoryReviewersPage'
import { DormitoryStudentsPage } from '../features/dormitory-admin/DormitoryStudentsPage'
import { NotificationPage } from '../features/notifications/NotificationPage'
import { GlobalAdmissionsPage } from '../features/global-admin/GlobalAdmissionsPage'
import { GlobalDashboardPage } from '../features/global-admin/GlobalDashboardPage'
import { GlobalDocumentTypesPage } from '../features/global-admin/GlobalDocumentTypesPage'
import { GlobalDormitoriesPage } from '../features/global-admin/GlobalDormitoriesPage'
import { GlobalRequirementsPage } from '../features/global-admin/GlobalRequirementsPage'
import { GlobalStudentsPage } from '../features/global-admin/GlobalStudentsPage'
import { GlobalTermsPage } from '../features/global-admin/GlobalTermsPage'
import { GlobalUsersPage } from '../features/global-admin/GlobalUsersPage'
import { MyReviewsPage } from '../features/reviewer/MyReviewsPage'
import { PendingDocumentsPage } from '../features/reviewer/PendingDocumentsPage'
import { ReviewerDashboardPage } from '../features/reviewer/ReviewerDashboardPage'
import { ReviewerStudentsGapPage } from '../features/reviewer/ReviewerStudentsGapPage'
import { StudentDashboardPage } from '../features/student/StudentDashboardPage'
import { StudentDocumentsPage } from '../features/student/StudentDocumentsPage'
import { AppLayout } from '../layouts/AppLayout'
import { ForbiddenPage } from '../pages/ForbiddenPage'
import { HomePage } from '../pages/HomePage'
import { ProfilePage } from '../pages/ProfilePage'
import { PublicOnlyRoute } from './PublicOnlyRoute'
import { RequireAdminScope } from './RequireAdminScope'
import { RequireAuth } from './RequireAuth'
import { RequireRole } from './RequireRole'

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
          <Route path="/notifications" element={<NotificationPage />} />
          <Route path="/forbidden" element={<ForbiddenPage />} />

          <Route element={<RequireRole role="STUDENT" />}>
            <Route
              path="/student/dashboard"
              element={<StudentDashboardPage />}
            />
            <Route
              path="/student/documents"
              element={<StudentDocumentsPage />}
            />
          </Route>

          <Route element={<RequireRole role="REVIEWER" />}>
            <Route
              path="/reviewer/dashboard"
              element={<ReviewerDashboardPage />}
            />
            <Route
              path="/reviewer/documents"
              element={<PendingDocumentsPage />}
            />
            <Route
              path="/reviewer/students"
              element={<ReviewerStudentsGapPage />}
            />
            <Route
              path="/reviewer/reviews"
              element={<MyReviewsPage />}
            />
          </Route>

          <Route element={<RequireRole role="ADMIN" />}>
            <Route element={<RequireAdminScope scope="DORMITORY" />}>
              <Route
                path="/admin/dashboard"
                element={<DormitoryAdminDashboardPage />}
              />
              <Route
                path="/admin/admissions"
                element={<DormitoryAdmissionsPage />}
              />
              <Route
                path="/admin/students"
                element={<DormitoryStudentsPage />}
              />
              <Route
                path="/admin/reviewers"
                element={<DormitoryReviewersPage />}
              />
              <Route
                path="/admin/documents"
                element={<DormitoryDocumentsPage />}
              />
            </Route>

            <Route element={<RequireAdminScope scope="GLOBAL" />}>
              <Route
                path="/global/dashboard"
                element={<GlobalDashboardPage />}
              />
              <Route
                path="/global/dormitories"
                element={<GlobalDormitoriesPage />}
              />
              <Route
                path="/global/users"
                element={<GlobalUsersPage />}
              />
              <Route
                path="/global/students"
                element={<GlobalStudentsPage />}
              />
              <Route
                path="/global/admissions"
                element={<GlobalAdmissionsPage />}
              />
              <Route
                path="/global/terms"
                element={<GlobalTermsPage />}
              />
              <Route
                path="/global/document-types"
                element={<GlobalDocumentTypesPage />}
              />
              <Route
                path="/global/document-requirements"
                element={<GlobalRequirementsPage />}
              />
            </Route>
          </Route>

          <Route path="/" element={<Navigate to="/home" replace />} />
          <Route path="*" element={<Navigate to="/home" replace />} />
        </Route>
      </Route>
    </Routes>
  )
}
