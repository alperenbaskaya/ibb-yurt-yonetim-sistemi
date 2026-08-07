import type {
  AdmissionResponse,
  CreateReviewerRequest,
  DormitoryAdminDashboardResponse,
  DormitoryAdmissionDocuments,
  DownloadedAdminDocument,
  StudentResponse,
  UpdateAdmissionStatusRequest,
  UpdateReviewerRequest,
  UserResponse,
} from '../types/dormitoryAdmin'
import { httpClient } from './httpClient'

export async function getDormitoryAdminDashboard(): Promise<DormitoryAdminDashboardResponse> {
  const response = await httpClient.get<DormitoryAdminDashboardResponse>(
    '/admin-dashboard/me/dormitory',
  )
  return response.data
}

export async function getCurrentTermDormitoryAdmissions(): Promise<AdmissionResponse[]> {
  const response = await httpClient.get<AdmissionResponse[]>(
    '/admissions/current-term',
  )
  return response.data
}

export async function updateDormitoryAdmissionStatus(
  admissionId: number,
  request: UpdateAdmissionStatusRequest,
): Promise<AdmissionResponse> {
  const response = await httpClient.patch<AdmissionResponse>(
    `/admissions/${admissionId}/status`,
    request,
  )
  return response.data
}

export async function getDormitoryStudents(): Promise<StudentResponse[]> {
  const response = await httpClient.get<StudentResponse[]>('/students')
  return response.data
}

export async function getDormitoryReviewers(): Promise<UserResponse[]> {
  const response = await httpClient.get<UserResponse[]>('/users')
  return response.data
}

export async function createDormitoryReviewer(
  request: CreateReviewerRequest,
): Promise<UserResponse> {
  const response = await httpClient.post<UserResponse>('/users', request)
  return response.data
}

export async function updateDormitoryReviewer(
  reviewerId: number,
  request: UpdateReviewerRequest,
): Promise<UserResponse> {
  const response = await httpClient.put<UserResponse>(
    `/users/${reviewerId}`,
    request,
  )
  return response.data
}

export async function getAdmissionDocuments(
  admissionId: number,
): Promise<DormitoryAdmissionDocuments> {
  const response = await httpClient.get<DormitoryAdmissionDocuments>(
    `/student-documents/admission/${admissionId}`,
  )
  return response.data
}

export async function downloadAdminDocument(
  documentId: number,
): Promise<DownloadedAdminDocument> {
  const response = await httpClient.get<Blob>(
    `/student-documents/${documentId}/download`,
    { responseType: 'blob' },
  )
  return {
    blob: response.data,
    contentDisposition: response.headers['content-disposition'],
  }
}
