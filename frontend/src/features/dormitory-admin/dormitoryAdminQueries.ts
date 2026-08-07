import axios from 'axios'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
  createDormitoryReviewer,
  downloadAdminDocument,
  getAdmissionDocuments,
  getCurrentTermDormitoryAdmissions,
  getDormitoryAdminDashboard,
  getDormitoryReviewers,
  getDormitoryStudents,
  updateDormitoryAdmissionStatus,
  updateDormitoryReviewer,
} from '../../api/dormitoryAdminApi'
import type {
  CreateReviewerRequest,
  UpdateAdmissionStatusRequest,
  UpdateReviewerRequest,
} from '../../types/dormitoryAdmin'

export const dormitoryAdminQueryKeys = {
  root: (userId: number) => ['dormitory-admin', userId] as const,
  dashboard: (userId: number) => [...dormitoryAdminQueryKeys.root(userId), 'dashboard'] as const,
  admissions: (userId: number) => [...dormitoryAdminQueryKeys.root(userId), 'admissions'] as const,
  students: (userId: number) => [...dormitoryAdminQueryKeys.root(userId), 'students'] as const,
  reviewers: (userId: number) => [...dormitoryAdminQueryKeys.root(userId), 'reviewers'] as const,
  documents: (userId: number, admissionId: number) => [...dormitoryAdminQueryKeys.root(userId), 'documents', admissionId] as const,
}

export function useDormitoryAdminDashboard(userId: number) {
  return useQuery({ queryKey: dormitoryAdminQueryKeys.dashboard(userId), queryFn: getDormitoryAdminDashboard })
}

export function useDormitoryAdmissions(userId: number) {
  return useQuery({ queryKey: dormitoryAdminQueryKeys.admissions(userId), queryFn: getCurrentTermDormitoryAdmissions })
}

export function useDormitoryStudents(userId: number) {
  return useQuery({ queryKey: dormitoryAdminQueryKeys.students(userId), queryFn: getDormitoryStudents })
}

export function useDormitoryReviewers(userId: number) {
  return useQuery({ queryKey: dormitoryAdminQueryKeys.reviewers(userId), queryFn: getDormitoryReviewers })
}

export function useAdmissionDocuments(userId: number, admissionId: number | null) {
  return useQuery({
    queryKey: dormitoryAdminQueryKeys.documents(userId, admissionId ?? 0),
    queryFn: () => getAdmissionDocuments(admissionId!),
    enabled: admissionId !== null,
  })
}

export function useUpdateAdmissionStatus(userId: number) {
  const queryClient = useQueryClient()
  const invalidate = () => queryClient.invalidateQueries({ queryKey: dormitoryAdminQueryKeys.root(userId) })
  return useMutation({
    mutationFn: ({ admissionId, request }: { admissionId: number; request: UpdateAdmissionStatusRequest }) => updateDormitoryAdmissionStatus(admissionId, request),
    onSuccess: invalidate,
    onError: async (error: unknown) => {
      if (axios.isAxiosError(error) && error.response?.status === 409) await invalidate()
    },
  })
}

export function useCreateReviewer(userId: number) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (request: CreateReviewerRequest) => createDormitoryReviewer(request),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: dormitoryAdminQueryKeys.root(userId) }),
  })
}

export function useUpdateReviewer(userId: number) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ reviewerId, request }: { reviewerId: number; request: UpdateReviewerRequest }) => updateDormitoryReviewer(reviewerId, request),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: dormitoryAdminQueryKeys.root(userId) }),
  })
}

export function useDownloadAdminDocument() {
  return useMutation({ mutationFn: downloadAdminDocument })
}
