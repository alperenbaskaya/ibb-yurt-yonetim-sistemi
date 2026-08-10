import type { AdminStudentDocumentInspectionResponse } from '../types/adminStudentInspection'
import { httpClient } from './httpClient'

export async function getAdminStudentDocumentInspection(
  studentId: number,
): Promise<AdminStudentDocumentInspectionResponse> {
  const response = await httpClient.get<AdminStudentDocumentInspectionResponse>(
    `/students/${studentId}/document-inspection`,
  )
  return response.data
}

export async function downloadInspectedStudentDocument(documentId: number) {
  const response = await httpClient.get<Blob>(
    `/student-documents/${documentId}/download`,
    { responseType: 'blob' },
  )
  return response.data
}
