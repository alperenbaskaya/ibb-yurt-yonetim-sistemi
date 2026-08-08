import { Navigate, Route, Routes } from 'react-router-dom'
import { MyReviewsPage } from './MyReviewsPage'
import { PendingDocumentsPage } from './PendingDocumentsPage'
import { ReviewerDashboardPage } from './ReviewerDashboardPage'
import { ReviewerStudentsGapPage } from './ReviewerStudentsGapPage'

export default function ReviewerRoutes() {
  return (
    <Routes>
      <Route path="dashboard" element={<ReviewerDashboardPage />} />
      <Route path="documents" element={<PendingDocumentsPage />} />
      <Route path="students" element={<ReviewerStudentsGapPage />} />
      <Route path="reviews" element={<MyReviewsPage />} />
      <Route path="*" element={<Navigate to="/home" replace />} />
    </Routes>
  )
}
