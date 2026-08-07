import type { AdminScope, Role } from './auth'
import type { AdmissionStatus, StudentDocumentResponse } from './student'
import type {
  ActionRequiredStudentResponse,
  PendingDocumentTypeCountResponse,
} from './reviewer'

export interface DormitoryReviewerWorkloadResponse {
  reviewerId: number
  firstName: string
  lastName: string
  email: string
  active: boolean
  approvedReviewCount: number
  rejectedReviewCount: number
  revisionRequiredReviewCount: number
  totalReviewCount: number
}

export interface DormitoryAdminDashboardResponse {
  adminId: number
  firstName: string
  lastName: string
  email: string
  dormitoryId: number
  dormitoryName: string
  activeTermId: number
  activeTermName: string
  capacity: number
  activeStudentCount: number
  availableCapacity: number
  occupancyPercentage: number
  totalAdmissionCount: number
  pendingAdmissionCount: number
  approvedAdmissionCount: number
  rejectedAdmissionCount: number
  completedStudentCount: number
  incompleteStudentCount: number
  studentCompletionPercentage: number
  totalReviewerCount: number
  activeReviewerCount: number
  inactiveReviewerCount: number
  pendingDocumentCount: number
  approvedDocumentCount: number
  rejectedDocumentCount: number
  revisionRequiredDocumentCount: number
  reviewerWorkloads: DormitoryReviewerWorkloadResponse[]
  pendingDocumentsByType: PendingDocumentTypeCountResponse[]
  actionRequiredStudents: ActionRequiredStudentResponse[]
}

export interface AdmissionResponse {
  id: number
  studentId: number
  studentFirstName: string
  studentLastName: string
  identityNumber: string
  dormitoryTermId: number
  dormitoryTermName: string
  dormitoryId: number
  dormitoryName: string
  admissionDate: string
  status: AdmissionStatus
  createdAt: string
  updatedAt: string
}

export interface UpdateAdmissionStatusRequest {
  status: AdmissionStatus
}

export interface StudentResponse {
  id: number
  identityNumber: string
  faculty: string
  department: string
  phone: string
  birthDate: string
  userId: number
  firstName: string
  lastName: string
  email: string
}

export interface UserResponse {
  id: number
  firstName: string
  lastName: string
  email: string
  role: Role
  active: boolean
  adminScope: AdminScope | null
  dormitoryId: number | null
  dormitoryName: string | null
}

export interface CreateReviewerRequest {
  firstName: string
  lastName: string
  email: string
  password: string
  role: 'REVIEWER'
  adminScope: null
  dormitoryId: number
  active: boolean
}

export interface UpdateReviewerRequest {
  firstName: string
  lastName: string
  email: string
  role: 'REVIEWER'
  adminScope: null
  dormitoryId: number
  active: boolean
}

export interface DownloadedAdminDocument {
  blob: Blob
  contentDisposition: string | undefined
}

export type DormitoryAdmissionDocuments = StudentDocumentResponse[]
