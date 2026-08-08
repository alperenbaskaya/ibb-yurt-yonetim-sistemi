import { Navigate, Route, Routes } from 'react-router-dom'
import { DormitoryAdminDashboardPage } from './DormitoryAdminDashboardPage'
import { DormitoryAdmissionsPage } from './DormitoryAdmissionsPage'
import { DormitoryDocumentsPage } from './DormitoryDocumentsPage'
import { DormitoryReviewersPage } from './DormitoryReviewersPage'
import { DormitoryStudentsPage } from './DormitoryStudentsPage'

export default function DormitoryAdminRoutes() {
  return (
    <Routes>
      <Route path="dashboard" element={<DormitoryAdminDashboardPage />} />
      <Route path="admissions" element={<DormitoryAdmissionsPage />} />
      <Route path="students" element={<DormitoryStudentsPage />} />
      <Route path="reviewers" element={<DormitoryReviewersPage />} />
      <Route path="documents" element={<DormitoryDocumentsPage />} />
      <Route path="*" element={<Navigate to="/home" replace />} />
    </Routes>
  )
}
