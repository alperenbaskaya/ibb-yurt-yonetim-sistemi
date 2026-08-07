import type { AdminScope, Role } from './auth'
import type { AdmissionResponse, StudentResponse, UserResponse } from './dormitoryAdmin'

export type DormitoryAdmissionProcessStatus = 'NOT_STARTED' | 'IN_PROGRESS' | 'COMPLETED'

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
