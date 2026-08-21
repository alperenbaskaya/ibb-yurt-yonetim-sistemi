import { useQuery } from '@tanstack/react-query'
import * as api from '../../api/globalHistoryApi'
import type { AuditCategory } from '../../types/globalAdmin'

type DormitoryOperationCategory = Extract<AuditCategory, 'STUDENT_ACTIVITY' | 'REVIEWER_ACTIVITY'>

export const globalHistoryKeys = {
  root: (userId: number) => ['global-history', userId] as const,
  dormitory: (
    userId: number,
    dormitoryId: number,
    category: DormitoryOperationCategory,
    page: number,
    query: string,
  ) => [...globalHistoryKeys.root(userId), 'dormitory', dormitoryId, category, query, page] as const,
  system: (userId: number, page: number, query: string) =>
    [...globalHistoryKeys.root(userId), 'system', query, page] as const,
}

export function useDormitoryOperationHistory(
  userId: number,
  dormitoryId: number | null,
  category: DormitoryOperationCategory,
  page: number,
  query: string,
  enabled: boolean,
) {
  return useQuery({
    queryKey: globalHistoryKeys.dormitory(userId, dormitoryId ?? 0, category, page, query),
    queryFn: () => api.getDormitoryOperationHistory(dormitoryId!, category, page, query),
    enabled: enabled && dormitoryId !== null,
  })
}

export function useSystemManagementHistory(
  userId: number,
  page: number,
  query: string,
  enabled: boolean,
) {
  return useQuery({
    queryKey: globalHistoryKeys.system(userId, page, query),
    queryFn: () => api.getSystemManagementHistory(page, query),
    enabled,
  })
}
