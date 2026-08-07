import axios from 'axios'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
  downloadStudentDocument,
  getMyDocumentRequirements,
  getMyStudentDashboard,
  uploadMyStudentDocument,
} from '../../api/studentApi'
import type { StudentDocumentUploadRequest } from '../../types/student'

export const studentQueryKeys = {
  root: (userId: number) => ['student', userId] as const,
  dashboard: (userId: number) =>
    [...studentQueryKeys.root(userId), 'dashboard'] as const,
  requirements: (userId: number) =>
    [...studentQueryKeys.root(userId), 'document-requirements'] as const,
}

export function useStudentDashboard(userId: number) {
  return useQuery({
    queryKey: studentQueryKeys.dashboard(userId),
    queryFn: getMyStudentDashboard,
  })
}

export function useStudentDocumentRequirements(userId: number) {
  return useQuery({
    queryKey: studentQueryKeys.requirements(userId),
    queryFn: getMyDocumentRequirements,
  })
}

export function useUploadStudentDocument(userId: number) {
  const queryClient = useQueryClient()

  const invalidateStudentState = () =>
    queryClient.invalidateQueries({ queryKey: studentQueryKeys.root(userId) })

  return useMutation({
    mutationFn: (request: StudentDocumentUploadRequest) =>
      uploadMyStudentDocument(request),
    onSuccess: invalidateStudentState,
    onError: async (error: unknown) => {
      if (axios.isAxiosError(error) && error.response?.status === 409) {
        await invalidateStudentState()
      }
    },
  })
}

export function useDownloadStudentDocument() {
  return useMutation({ mutationFn: downloadStudentDocument })
}
