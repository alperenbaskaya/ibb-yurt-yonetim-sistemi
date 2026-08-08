import { Navigate, Route, Routes } from 'react-router-dom'
import { StudentDashboardPage } from './StudentDashboardPage'
import { StudentDocumentsPage } from './StudentDocumentsPage'

export default function StudentRoutes() {
  return (
    <Routes>
      <Route path="dashboard" element={<StudentDashboardPage />} />
      <Route path="documents" element={<StudentDocumentsPage />} />
      <Route path="*" element={<Navigate to="/home" replace />} />
    </Routes>
  )
}
