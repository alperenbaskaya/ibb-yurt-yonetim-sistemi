import { Navigate, Route, Routes } from 'react-router-dom'
import { FeaturePlaceholderPage } from '../components/common/FeaturePlaceholderPage'
import { LoginPage } from '../features/auth/LoginPage'
import { NotificationPage } from '../features/notifications/NotificationPage'
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

const placeholderDescriptions = {
  dashboard: 'Rolünüze ait operasyonel özet ve iş akışları bu alanda sunulacaktır.',
  documents: 'Belge süreçleri ve ilgili işlemler bu alanda yönetilecektir.',
  students: 'Öğrenci kayıtları ve ilgili süreçler bu alanda sunulacaktır.',
  management: 'Bu yönetim modülü sonraki geliştirme adımında kullanıma açılacaktır.',
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
              element={
                <FeaturePlaceholderPage
                  title="Kontrol Paneli"
                  description={placeholderDescriptions.dashboard}
                />
              }
            />
            <Route
              path="/reviewer/documents"
              element={
                <FeaturePlaceholderPage
                  title="Bekleyen Belgeler"
                  description="Değerlendirme bekleyen öğrenci belgeleri bu alanda sunulacaktır."
                />
              }
            />
            <Route
              path="/reviewer/students"
              element={
                <FeaturePlaceholderPage
                  title="Öğrenciler"
                  description={placeholderDescriptions.students}
                />
              }
            />
            <Route
              path="/reviewer/reviews"
              element={
                <FeaturePlaceholderPage
                  title="Değerlendirmelerim"
                  description="Gerçekleştirdiğiniz belge değerlendirmeleri bu alanda listelenecektir."
                />
              }
            />
          </Route>

          <Route element={<RequireRole role="ADMIN" />}>
            <Route element={<RequireAdminScope scope="DORMITORY" />}>
              <Route
                path="/admin/dashboard"
                element={
                  <FeaturePlaceholderPage
                    title="Kontrol Paneli"
                    description={placeholderDescriptions.dashboard}
                  />
                }
              />
              <Route
                path="/admin/admissions"
                element={
                  <FeaturePlaceholderPage
                    title="Kabuller"
                    description="Yurdunuza ait kabul kayıtları ve karar süreçleri bu alanda yönetilecektir."
                  />
                }
              />
              <Route
                path="/admin/students"
                element={
                  <FeaturePlaceholderPage
                    title="Öğrenciler"
                    description={placeholderDescriptions.students}
                  />
                }
              />
              <Route
                path="/admin/reviewers"
                element={
                  <FeaturePlaceholderPage
                    title="Değerlendiriciler"
                    description="Yurdunuza bağlı belge değerlendiricileri bu alanda yönetilecektir."
                  />
                }
              />
              <Route
                path="/admin/documents"
                element={
                  <FeaturePlaceholderPage
                    title="Belgeler"
                    description={placeholderDescriptions.documents}
                  />
                }
              />
            </Route>

            <Route element={<RequireAdminScope scope="GLOBAL" />}>
              <Route
                path="/global/dashboard"
                element={
                  <FeaturePlaceholderPage
                    title="Genel Kontrol Paneli"
                    description={placeholderDescriptions.dashboard}
                  />
                }
              />
              <Route
                path="/global/dormitories"
                element={
                  <FeaturePlaceholderPage
                    title="Yurtlar"
                    description="Sistemde tanımlı yurtlar bu alanda yönetilecektir."
                  />
                }
              />
              <Route
                path="/global/users"
                element={
                  <FeaturePlaceholderPage
                    title="Kullanıcılar"
                    description="Sistem kullanıcıları ve yetki tanımları bu alanda yönetilecektir."
                  />
                }
              />
              <Route
                path="/global/students"
                element={
                  <FeaturePlaceholderPage
                    title="Öğrenciler"
                    description={placeholderDescriptions.students}
                  />
                }
              />
              <Route
                path="/global/admissions"
                element={
                  <FeaturePlaceholderPage
                    title="Kabuller"
                    description="Tüm yurtlara ait kabul süreçleri bu alanda yönetilecektir."
                  />
                }
              />
              <Route
                path="/global/terms"
                element={
                  <FeaturePlaceholderPage
                    title="Yurt Dönemleri"
                    description={placeholderDescriptions.management}
                  />
                }
              />
              <Route
                path="/global/document-types"
                element={
                  <FeaturePlaceholderPage
                    title="Belge Türleri"
                    description={placeholderDescriptions.management}
                  />
                }
              />
              <Route
                path="/global/document-requirements"
                element={
                  <FeaturePlaceholderPage
                    title="Belge Gereksinimleri"
                    description={placeholderDescriptions.management}
                  />
                }
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
