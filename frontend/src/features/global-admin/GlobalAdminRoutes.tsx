import { Navigate, Route, Routes } from 'react-router-dom'
import { GlobalAdmissionsPage } from './GlobalAdmissionsPage'
import { GlobalDashboardPage } from './GlobalDashboardPage'
import { GlobalDocumentTypesPage } from './GlobalDocumentTypesPage'
import { GlobalDormitoriesPage } from './GlobalDormitoriesPage'
import { GlobalRequirementsPage } from './GlobalRequirementsPage'
import { GlobalStudentsPage } from './GlobalStudentsPage'
import { GlobalTermsPage } from './GlobalTermsPage'
import { GlobalUsersPage } from './GlobalUsersPage'
import { GlobalHistoryPage } from './GlobalHistoryPage'

export default function GlobalAdminRoutes() {
  return (
    <Routes>
      <Route path="dashboard" element={<GlobalDashboardPage />} />
      <Route path="dormitories" element={<GlobalDormitoriesPage />} />
      <Route path="users" element={<GlobalUsersPage />} />
      <Route path="students" element={<GlobalStudentsPage />} />
      <Route path="admissions" element={<GlobalAdmissionsPage />} />
      <Route path="terms" element={<GlobalTermsPage />} />
      <Route path="document-types" element={<GlobalDocumentTypesPage />} />
      <Route path="document-requirements" element={<GlobalRequirementsPage />} />
      <Route path="history" element={<GlobalHistoryPage />} />
      <Route path="*" element={<Navigate to="/home" replace />} />
    </Routes>
  )
}
