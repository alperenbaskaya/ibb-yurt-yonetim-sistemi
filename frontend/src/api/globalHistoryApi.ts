import type { AuditCategory, AuditLogPageResponse } from '../types/globalAdmin'
import { httpClient } from './httpClient'

type DormitoryOperationCategory = Extract<AuditCategory, 'STUDENT_ACTIVITY' | 'REVIEWER_ACTIVITY'>

export async function getDormitoryOperationHistory(
  dormitoryId: number,
  category: DormitoryOperationCategory,
  page: number,
): Promise<AuditLogPageResponse> {
  return (await httpClient.get<AuditLogPageResponse>('/global-history/dormitory-operations', {
    params: { dormitoryId, category, page, size: 20 },
  })).data
}

export async function getSystemManagementHistory(page: number): Promise<AuditLogPageResponse> {
  return (await httpClient.get<AuditLogPageResponse>('/global-history/system-management', {
    params: { page, size: 20 },
  })).data
}
