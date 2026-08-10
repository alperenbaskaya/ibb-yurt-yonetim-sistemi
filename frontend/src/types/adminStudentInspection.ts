import type { DocumentReviewDecision, StudentDocumentStatus } from './student'

export interface AdminDocumentReviewTraceResponse {
  reviewerUserId: number
  reviewerFirstName: string
  reviewerLastName: string
  decision: DocumentReviewDecision
  comment: string | null
  reviewedAt: string
}

export interface AdminStudentDocumentInspectionItemResponse {
  documentTypeId: number
  documentTypeName: string
  required: boolean
  uploaded: boolean
  studentDocumentId: number | null
  status: StudentDocumentStatus | null
  originalFileName: string | null
  uploadedAt: string | null
  latestReview: AdminDocumentReviewTraceResponse | null
  reviewHistory: AdminDocumentReviewTraceResponse[]
}

export interface AdminStudentDocumentInspectionResponse {
  studentId: number
  studentFirstName: string
  studentLastName: string
  identityNumber: string
  dormitoryId: number
  dormitoryName: string
  dormitoryTermId: number
  dormitoryTermName: string
  totalRequiredDocumentCount: number
  approvedRequiredDocumentCount: number
  missingRequiredDocumentCount: number
  uploadedRequiredDocumentCount: number
  revisionRequiredDocumentCount: number
  rejectedDocumentCount: number
  completed: boolean
  requiredDocuments: AdminStudentDocumentInspectionItemResponse[]
}
