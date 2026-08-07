import axios from 'axios'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
  createDocumentReview,
  downloadReviewerDocument,
  getDocumentReviewHistory,
  getMyDocumentReviews,
  getMyReviewerDashboard,
  getPendingReviewerDocuments,
  getReviewerDocument,
} from '../../api/reviewerApi'
import type { CreateDocumentReviewRequest } from '../../types/reviewer'

export const reviewerQueryKeys = {
  root: (userId: number) => ['reviewer', userId] as const,
  dashboard: (userId: number) =>
    [...reviewerQueryKeys.root(userId), 'dashboard'] as const,
  pendingDocuments: (userId: number) =>
    [...reviewerQueryKeys.root(userId), 'pending-documents'] as const,
  myReviews: (userId: number) =>
    [...reviewerQueryKeys.root(userId), 'my-reviews'] as const,
  document: (userId: number, documentId: number) =>
    [...reviewerQueryKeys.root(userId), 'document', documentId] as const,
  documentReviews: (userId: number, documentId: number) =>
    [...reviewerQueryKeys.root(userId), 'document-reviews', documentId] as const,
}

export function useReviewerDashboard(userId: number) {
  return useQuery({
    queryKey: reviewerQueryKeys.dashboard(userId),
    queryFn: getMyReviewerDashboard,
  })
}

export function usePendingReviewerDocuments(userId: number) {
  return useQuery({
    queryKey: reviewerQueryKeys.pendingDocuments(userId),
    queryFn: getPendingReviewerDocuments,
  })
}

export function useReviewerDocument(userId: number, documentId: number | null) {
  return useQuery({
    queryKey: reviewerQueryKeys.document(userId, documentId ?? 0),
    queryFn: () => getReviewerDocument(documentId!),
    enabled: documentId !== null,
  })
}

export function useDocumentReviewHistory(userId: number, documentId: number | null) {
  return useQuery({
    queryKey: reviewerQueryKeys.documentReviews(userId, documentId ?? 0),
    queryFn: () => getDocumentReviewHistory(documentId!),
    enabled: documentId !== null,
  })
}

export function useMyDocumentReviews(userId: number) {
  return useQuery({
    queryKey: reviewerQueryKeys.myReviews(userId),
    queryFn: getMyDocumentReviews,
  })
}

export function useCreateDocumentReview(userId: number) {
  const queryClient = useQueryClient()

  const invalidateReviewerState = () =>
    queryClient.invalidateQueries({ queryKey: reviewerQueryKeys.root(userId) })

  return useMutation({
    mutationFn: (request: CreateDocumentReviewRequest) =>
      createDocumentReview(request),
    onSuccess: invalidateReviewerState,
    onError: async (error: unknown) => {
      if (axios.isAxiosError(error) && error.response?.status === 409) {
        await invalidateReviewerState()
      }
    },
  })
}

export function useDownloadReviewerDocument() {
  return useMutation({ mutationFn: downloadReviewerDocument })
}
