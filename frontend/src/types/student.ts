export type AdmissionStatus = 'PENDING' | 'APPROVED' | 'REJECTED'

export type UploadPeriodStatus = 'NOT_STARTED' | 'OPEN' | 'CLOSED'

export type StudentDocumentStatus =
  | 'UPLOADED'
  | 'APPROVED'
  | 'REJECTED'
  | 'REVISION_REQUIRED'

export type StudentDocumentActionReason =
  | 'MISSING'
  | 'REVISION_REQUIRED'
  | 'REJECTED'

export type DocumentReviewDecision =
  | 'APPROVED'
  | 'REJECTED'
  | 'REVISION_REQUIRED'

export interface StudentDocumentRequirementStatusResponse {
  requirementId: number
  documentTypeId: number
  documentTypeName: string
  documentTypeDescription: string | null
  required: boolean
  uploaded: boolean
  studentDocumentId: number | null
  originalFileName: string | null
  status: StudentDocumentStatus | null
  uploadedAt: string | null
}

export interface StudentDocumentActionRequiredResponse {
  documentTypeId: number
  documentTypeName: string
  studentDocumentId: number | null
  reason: StudentDocumentActionReason
  message: string
}

export interface StudentLastReviewResponse {
  reviewId: number
  studentDocumentId: number
  documentTypeId: number
  documentTypeName: string
  decision: DocumentReviewDecision
  comment: string | null
  reviewedAt: string
}

export interface StudentDashboardResponse {
  studentId: number
  userId: number
  firstName: string
  lastName: string
  email: string
  admissionId: number
  admissionStatus: AdmissionStatus
  admissionStatusMessage: string
  dormitoryId: number
  dormitoryName: string
  dormitoryTermId: number
  dormitoryTermName: string
  documentUploadStartDate: string
  documentUploadEndDate: string
  remainingUploadDays: number
  uploadPeriodStatus: UploadPeriodStatus
  totalRequiredDocuments: number
  approvedRequiredDocuments: number
  completionPercentage: number
  documentProcessCompleted: boolean
  lastReview: StudentLastReviewResponse | null
  actionRequiredDocuments: StudentDocumentActionRequiredResponse[]
  documents: StudentDocumentRequirementStatusResponse[]
}

export interface StudentDocumentResponse {
  id: number
  admissionId: number
  studentId: number
  studentFirstName: string
  studentLastName: string
  dormitoryTermId: number
  dormitoryTermName: string
  documentTypeId: number
  documentTypeName: string
  originalFileName: string
  contentType: string
  fileSize: number
  status: StudentDocumentStatus
  uploadedAt: string
  updatedAt: string
}

export interface StudentDocumentUploadRequest {
  documentTypeId: number
  file: File
}

export interface DownloadedStudentDocument {
  blob: Blob
  contentDisposition: string | undefined
}
