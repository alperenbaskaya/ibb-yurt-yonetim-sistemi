import type {
  DownloadedStudentDocument,
  StudentDashboardResponse,
  StudentDocumentRequirementStatusResponse,
  StudentDocumentResponse,
  StudentDocumentUploadRequest,
} from '../types/student'
import { httpClient } from './httpClient'

export async function getMyStudentDashboard(): Promise<StudentDashboardResponse> {
  const response = await httpClient.get<StudentDashboardResponse>(
    '/student-dashboard/me',
  )
  return response.data
}

export async function getMyDocumentRequirements(): Promise<
  StudentDocumentRequirementStatusResponse[]
> {
  const response = await httpClient.get<
    StudentDocumentRequirementStatusResponse[]
  >('/student-documents/me/requirements')
  return response.data
}

export async function uploadMyStudentDocument({
  documentTypeId,
  file,
}: StudentDocumentUploadRequest): Promise<StudentDocumentResponse> {
  const formData = new FormData()
  formData.append('file', file)

  const response = await httpClient.post<StudentDocumentResponse>(
    '/student-documents/me/upload',
    formData,
    { params: { documentTypeId } },
  )
  return response.data
}

export async function downloadStudentDocument(
  studentDocumentId: number,
): Promise<DownloadedStudentDocument> {
  const response = await httpClient.get<Blob>(
    `/student-documents/${studentDocumentId}/download`,
    { responseType: 'blob' },
  )

  return {
    blob: response.data,
    contentDisposition: response.headers['content-disposition'],
  }
}
