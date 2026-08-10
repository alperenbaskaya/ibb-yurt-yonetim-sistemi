import type { StudentDocumentResponse } from '../types/student'
import type {
  CreateDocumentReviewRequest,
  DocumentReviewResponse,
  DownloadedReviewerDocument,
  ReviewerDashboardResponse,
  ReviewerStudentResponse,
} from '../types/reviewer'
import { httpClient } from './httpClient'

export async function getMyReviewerDashboard(): Promise<ReviewerDashboardResponse> {
  const response = await httpClient.get<ReviewerDashboardResponse>(
    '/reviewer-dashboard/me',
  )
  return response.data
}

export async function getMyReviewerStudents(): Promise<ReviewerStudentResponse[]> {
  const response = await httpClient.get<ReviewerStudentResponse[]>(
    '/reviewer-students/me',
  )
  return response.data
}

export async function getPendingReviewerDocuments(): Promise<StudentDocumentResponse[]> {
  const response = await httpClient.get<StudentDocumentResponse[]>(
    '/student-documents/pending',
  )
  return response.data
}

export async function getReviewerDocument(
  studentDocumentId: number,
): Promise<StudentDocumentResponse> {
  const response = await httpClient.get<StudentDocumentResponse>(
    `/student-documents/${studentDocumentId}`,
  )
  return response.data
}

export async function getDocumentReviewHistory(
  studentDocumentId: number,
): Promise<DocumentReviewResponse[]> {
  const response = await httpClient.get<DocumentReviewResponse[]>(
    `/document-reviews/document/${studentDocumentId}`,
  )
  return response.data
}

export async function getMyDocumentReviews(): Promise<DocumentReviewResponse[]> {
  const response = await httpClient.get<DocumentReviewResponse[]>(
    '/document-reviews/me',
  )
  return response.data
}

export async function createDocumentReview(
  request: CreateDocumentReviewRequest,
): Promise<DocumentReviewResponse> {
  const response = await httpClient.post<DocumentReviewResponse>(
    '/document-reviews',
    request,
  )
  return response.data
}

export async function downloadReviewerDocument(
  studentDocumentId: number,
): Promise<DownloadedReviewerDocument> {
  const response = await httpClient.get<Blob>(
    `/student-documents/${studentDocumentId}/download`,
    { responseType: 'blob' },
  )
  return {
    blob: response.data,
    contentDisposition: response.headers['content-disposition'],
  }
}
