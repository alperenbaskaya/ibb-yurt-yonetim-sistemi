import type { AuditCategory, AuditLogPageResponse } from '../types/globalAdmin'
import { httpClient } from './httpClient'

export type DormitoryHistoryCategory = Extract<
  AuditCategory,
  'STUDENT_ACTIVITY' | 'REVIEWER_ACTIVITY'
>

export async function getDormitoryAdminHistory(
  category: DormitoryHistoryCategory,
  page: number,
): Promise<AuditLogPageResponse> {
  return (await httpClient.get<AuditLogPageResponse>('/admin-history/me/dormitory', {
    params: { category, page, size: 20 },
  })).data
}
