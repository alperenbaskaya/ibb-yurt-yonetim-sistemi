import type {
  DocumentReviewDecision,
  StudentDocumentStatus,
  StudentDocumentResponse,
} from './student'

export interface PendingDocumentTypeCountResponse {
  documentTypeId: number
  documentTypeName: string
  pendingCount: number
}

export interface ActionRequiredStudentResponse {
  studentId: number
  admissionId: number
  firstName: string
  lastName: string
  missingDocumentCount: number
  revisionRequiredDocumentCount: number
  rejectedDocumentCount: number
}

export interface DocumentReviewResponse {
  id: number
  studentDocumentId: number
  documentTypeName: string
  originalFileName: string
  studentId: number
  studentFirstName: string
  studentLastName: string
  reviewerUserId: number
  reviewerFirstName: string
  reviewerLastName: string
  decision: DocumentReviewDecision
  comment: string | null
  reviewedAt: string
}

export interface ReviewerDashboardResponse {
  reviewerId: number
  firstName: string
  lastName: string
  email: string
  dormitoryId: number
  dormitoryName: string
  activeTermId: number
  activeTermName: string
  activeStudentCount: number
  completedStudentCount: number
  incompleteStudentCount: number
  actionRequiredStudentCount: number
  studentCompletionPercentage: number
  pendingDocumentCount: number
  approvedDocumentCount: number
  rejectedDocumentCount: number
  revisionRequiredDocumentCount: number
  pendingDocumentsByType: PendingDocumentTypeCountResponse[]
  oldestPendingDocuments: StudentDocumentResponse[]
  actionRequiredStudents: ActionRequiredStudentResponse[]
  recentDormitoryReviews: DocumentReviewResponse[]
}

export interface ReviewerStudentResponse {
  studentId: number
  firstName: string
  lastName: string
  email: string
  identityNumber: string
  faculty: string
  department: string
  phone: string
}

export interface ReviewerStudentDocumentProcessItemResponse {
  documentTypeId: number
  documentTypeName: string
  uploaded: boolean
  status: StudentDocumentStatus | null
}

export interface ReviewerStudentDocumentProcessResponse {
  studentId: number
  firstName: string
  lastName: string
  identityNumber: string
  dormitoryTermId: number
  dormitoryTermName: string
  totalRequiredDocumentCount: number
  approvedRequiredDocumentCount: number
  missingRequiredDocumentCount: number
  uploadedRequiredDocumentCount: number
  revisionRequiredDocumentCount: number
  rejectedDocumentCount: number
  completed: boolean
  requiredDocuments: ReviewerStudentDocumentProcessItemResponse[]
}

export interface CreateDocumentReviewRequest {
  studentDocumentId: number
  decision: DocumentReviewDecision
  comment: string | null
}

export interface DownloadedReviewerDocument {
  blob: Blob
  contentDisposition: string | undefined
}
