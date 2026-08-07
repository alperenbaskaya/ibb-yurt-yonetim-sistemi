import type { AdmissionStatus } from '../types/student'
import type { UpdateAdmissionStatusRequest } from '../types/dormitoryAdmin'
import type {
  AdmissionResponse, CreateDocumentTypeRequest, CreateDormitoryRequest,
  CreateTermDocumentRequirementRequest, CreateUserRequest, DocumentTypeResponse,
  DormitoryResponse, DormitoryTermRequest, DormitoryTermResponse,
  GlobalAdminDashboardResponse, StudentResponse, TermDocumentRequirementResponse,
  UpdateDocumentTypeRequest, UpdateDormitoryRequest, UpdateUserRequest, UserResponse,
} from '../types/globalAdmin'
import { httpClient } from './httpClient'

export const getGlobalDashboard = async () => (await httpClient.get<GlobalAdminDashboardResponse>('/admin-dashboard/me/global')).data
export const getDormitories = async () => (await httpClient.get<DormitoryResponse[]>('/dormitories')).data
export const createDormitory = async (request: CreateDormitoryRequest) => (await httpClient.post<DormitoryResponse>('/dormitories', request)).data
export const updateDormitory = async (id: number, request: UpdateDormitoryRequest) => (await httpClient.put<DormitoryResponse>(`/dormitories/${id}`, request)).data
export const getGlobalUsers = async () => (await httpClient.get<UserResponse[]>('/users')).data
export const createGlobalUser = async (request: CreateUserRequest) => (await httpClient.post<UserResponse>('/users', request)).data
export const updateGlobalUser = async (id: number, request: UpdateUserRequest) => (await httpClient.put<UserResponse>(`/users/${id}`, request)).data
export const getGlobalStudents = async () => (await httpClient.get<StudentResponse[]>('/students')).data
export const getGlobalAdmissions = async (status?: AdmissionStatus) => (await httpClient.get<AdmissionResponse[]>('/admissions/current-term', { params: status ? { status } : undefined })).data
export const updateGlobalAdmissionStatus = async (id: number, request: UpdateAdmissionStatusRequest) => (await httpClient.patch<AdmissionResponse>(`/admissions/${id}/status`, request)).data
export const getDormitoryTerms = async () => (await httpClient.get<DormitoryTermResponse[]>('/dormitory-terms')).data
export const createDormitoryTerm = async (request: DormitoryTermRequest) => (await httpClient.post<DormitoryTermResponse>('/dormitory-terms', request)).data
export const updateDormitoryTerm = async (id: number, request: DormitoryTermRequest) => (await httpClient.put<DormitoryTermResponse>(`/dormitory-terms/${id}`, request)).data
export const setDormitoryTermActive = async (id: number, active: boolean) => (await httpClient.patch<DormitoryTermResponse>(`/dormitory-terms/${id}/active`, { active })).data
export const deleteDormitoryTerm = async (id: number): Promise<void> => { await httpClient.delete(`/dormitory-terms/${id}`) }
export const getDocumentTypes = async () => (await httpClient.get<DocumentTypeResponse[]>('/document-types')).data
export const createDocumentType = async (request: CreateDocumentTypeRequest) => (await httpClient.post<DocumentTypeResponse>('/document-types', request)).data
export const updateDocumentType = async (id: number, request: UpdateDocumentTypeRequest) => (await httpClient.put<DocumentTypeResponse>(`/document-types/${id}`, request)).data
export const getRequirementsByTerm = async (termId: number) => (await httpClient.get<TermDocumentRequirementResponse[]>(`/term-document-requirements/term/${termId}`)).data
export const createRequirement = async (request: CreateTermDocumentRequirementRequest) => (await httpClient.post<TermDocumentRequirementResponse>('/term-document-requirements', request)).data
export const updateRequirement = async (id: number, required: boolean) => (await httpClient.patch<TermDocumentRequirementResponse>(`/term-document-requirements/${id}`, { required })).data
