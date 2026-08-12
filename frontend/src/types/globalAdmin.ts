import type { AdminScope, Role } from './auth'
import type { AdmissionResponse, StudentResponse, UserResponse } from './dormitoryAdmin'

export type DormitoryAdmissionProcessStatus = 'NOT_STARTED' | 'IN_PROGRESS' | 'COMPLETED'

export type AuditCategory = 'STUDENT_ACTIVITY' | 'REVIEWER_ACTIVITY' | 'SYSTEM_MANAGEMENT'
export type AuditAction =
  | 'ADMISSION_APPROVED' | 'ADMISSION_REJECTED'
  | 'DOCUMENT_UPLOADED' | 'DOCUMENT_REUPLOADED' | 'DOCUMENT_PROCESS_COMPLETED'
  | 'DOCUMENT_APPROVED' | 'DOCUMENT_REJECTED' | 'DOCUMENT_REVISION_REQUIRED'
  | 'DORMITORY_CREATED' | 'DORMITORY_UPDATED'
  | 'USER_CREATED' | 'USER_UPDATED' | 'USER_ACTIVATED' | 'USER_DEACTIVATED'
  | 'TERM_CREATED' | 'TERM_UPDATED' | 'TERM_ACTIVATED' | 'TERM_DEACTIVATED' | 'TERM_DELETED'
  | 'DOCUMENT_TYPE_CREATED' | 'DOCUMENT_TYPE_UPDATED'
  | 'REQUIREMENT_CREATED' | 'REQUIREMENT_UPDATED'
export type AuditEntityType = 'ADMISSION' | 'STUDENT_DOCUMENT' | 'USER' | 'DORMITORY' | 'DORMITORY_TERM' | 'DOCUMENT_TYPE' | 'TERM_DOCUMENT_REQUIREMENT'

export interface AuditLogResponse {
  id: number
  actorUserId: number
  actorName: string
  actorRole: Role
  subjectStudentId: number | null
  subjectStudentName: string | null
  dormitoryId: number | null
  dormitoryName: string | null
  category: AuditCategory
  action: AuditAction
  entityType: AuditEntityType
  entityId: number
  targetLabel: string
  description: string
  createdAt: string
}

export interface AuditLogPageResponse {
  items: AuditLogResponse[]
  page: number
  size: number
  totalElements: number
  totalPages: number
  first: boolean
  last: boolean
}

export interface GlobalDormitoryManagerResponse {
  userId: number
  firstName: string
  lastName: string
  email: string
  active: boolean
}

export interface GlobalDormitoryReviewerResponse {
  reviewerId: number
  firstName: string
  lastName: string
  email: string
  active: boolean
}

export interface GlobalDormitoryStatisticsResponse {
  dormitoryId: number
  dormitoryName: string
  active: boolean
  manager: GlobalDormitoryManagerResponse | null
  totalReviewerCount: number
  activeReviewerCount: number
  inactiveReviewerCount: number
  reviewers: GlobalDormitoryReviewerResponse[]
  capacity: number
  activeStudentCount: number
  availableCapacity: number
  occupancyPercentage: number
  totalAdmissionCount: number
  pendingAdmissionCount: number
  approvedAdmissionCount: number
  rejectedAdmissionCount: number
  admissionProcessStatus: DormitoryAdmissionProcessStatus
  admissionProcessCompleted: boolean
  admissionProcessMessage: string
  completedStudentCount: number
  incompleteStudentCount: number
  actionRequiredStudentCount: number
  studentCompletionPercentage: number
  pendingDocumentCount: number
  approvedDocumentCount: number
  rejectedDocumentCount: number
  revisionRequiredDocumentCount: number
}

export interface GlobalAdminDashboardResponse {
  adminId: number; firstName: string; lastName: string; email: string
  activeTermId: number; activeTermName: string
  totalDormitoryCount: number; activeDormitoryCount: number; inactiveDormitoryCount: number
  totalCapacity: number; activeStudentCount: number; availableCapacity: number; occupancyPercentage: number
  totalAdmissionCount: number; pendingAdmissionCount: number; approvedAdmissionCount: number; rejectedAdmissionCount: number
  completedStudentCount: number; incompleteStudentCount: number; actionRequiredStudentCount: number; studentCompletionPercentage: number
  totalAdminCount: number; globalAdminCount: number; dormitoryAdminCount: number
  totalReviewerCount: number; activeReviewerCount: number; inactiveReviewerCount: number
  totalStudentUserCount: number
  pendingDocumentCount: number; approvedDocumentCount: number; rejectedDocumentCount: number; revisionRequiredDocumentCount: number
  admissionCompletedDormitoryCount: number; admissionInProgressDormitoryCount: number
  dormitoryStatistics: GlobalDormitoryStatisticsResponse[]
}

export interface AdmissionPageResponse {
  content: AdmissionResponse[]
  page: number
  size: number
  totalElements: number
  totalPages: number
  first: boolean
  last: boolean
}

export interface BulkApproveAdmissionsRequest {
  admissionIds: number[]
}

export interface BulkApproveAdmissionsResponse {
  approvedCount: number
}

export interface DormitoryResponse { id: number; name: string; address: string | null; capacity: number; active: boolean; createdAt: string; updatedAt: string }
export interface CreateDormitoryRequest { name: string; address: string | null; capacity: number }
export interface UpdateDormitoryRequest extends CreateDormitoryRequest { active: boolean }

export interface CreateUserRequest { firstName: string; lastName: string; email: string; password: string; role: Role; adminScope: AdminScope | null; dormitoryId: number | null; active: boolean }
export interface UpdateUserRequest { firstName: string; lastName: string; email: string; role: Role; adminScope: AdminScope | null; dormitoryId: number | null; active: boolean }

export interface DormitoryTermResponse { id: number; name: string; startDate: string; endDate: string; documentUploadStartDate: string; documentUploadEndDate: string; active: boolean }
export interface DormitoryTermRequest { name: string; startDate: string; endDate: string; documentUploadStartDate: string; documentUploadEndDate: string; active: boolean }

export interface DocumentTypeResponse { id: number; name: string; description: string | null; active: boolean; createdAt: string; updatedAt: string }
export interface CreateDocumentTypeRequest { name: string; description: string | null }
export interface UpdateDocumentTypeRequest extends CreateDocumentTypeRequest { active: boolean }

export interface TermDocumentRequirementResponse { id: number; dormitoryTermId: number; dormitoryTermName: string; documentTypeId: number; documentTypeName: string; documentTypeDescription: string | null; documentTypeActive: boolean; required: boolean; createdAt: string; updatedAt: string }
export interface CreateTermDocumentRequirementRequest { dormitoryTermId: number; documentTypeId: number; required: boolean }

export type { AdmissionResponse, StudentResponse, UserResponse }
