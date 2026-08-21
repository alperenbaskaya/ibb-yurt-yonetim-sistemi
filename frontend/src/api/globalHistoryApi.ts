import type { AuditCategory, AuditLogPageResponse } from '../types/globalAdmin'
import { HISTORY_PAGE_SIZE } from '../utils/pagination'
import { httpClient } from './httpClient'

type DormitoryOperationCategory = Extract<AuditCategory, 'STUDENT_ACTIVITY' | 'REVIEWER_ACTIVITY'>

export async function getDormitoryOperationHistory(
  dormitoryId: number,
  category: DormitoryOperationCategory,
  page: number,
  query: string,
): Promise<AuditLogPageResponse> {
  return (await httpClient.get<AuditLogPageResponse>('/global-history/dormitory-operations', {
    params: { dormitoryId, category, page, size: HISTORY_PAGE_SIZE, q: query || undefined },
  })).data
}

export async function getSystemManagementHistory(page: number, query: string): Promise<AuditLogPageResponse> {
  return (await httpClient.get<AuditLogPageResponse>('/global-history/system-management', {
    params: { page, size: HISTORY_PAGE_SIZE, q: query || undefined },
  })).data
}
