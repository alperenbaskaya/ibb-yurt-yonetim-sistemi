import { useMutation, useQuery } from '@tanstack/react-query'
import {
  downloadInspectedStudentDocument,
  getAdminStudentDocumentInspection,
} from '../../api/adminStudentInspectionApi'

export const adminStudentInspectionKeys = {
  detail: (userId: number, studentId: number) =>
    ['admin-student-inspection', userId, 'student', studentId] as const,
}

export function useAdminStudentInspection(userId: number, studentId: number | null) {
  return useQuery({
    queryKey: adminStudentInspectionKeys.detail(userId, studentId ?? 0),
    queryFn: () => getAdminStudentDocumentInspection(studentId!),
    enabled: studentId !== null,
  })
}

export function useDownloadInspectedStudentDocument() {
  return useMutation({ mutationFn: downloadInspectedStudentDocument })
}
