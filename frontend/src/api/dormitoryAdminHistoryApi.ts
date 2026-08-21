import type { AuditCategory, AuditLogPageResponse } from '../types/globalAdmin'
import { HISTORY_PAGE_SIZE } from '../utils/pagination'
import { httpClient } from './httpClient'

export type DormitoryHistoryCategory = Extract<
  AuditCategory,
  'STUDENT_ACTIVITY' | 'REVIEWER_ACTIVITY'
>

export async function getDormitoryAdminHistory(
  category: DormitoryHistoryCategory,
  page: number,
  query: string,
): Promise<AuditLogPageResponse> {
  return (await httpClient.get<AuditLogPageResponse>('/admin-history/me/dormitory', {
    params: { category, page, size: HISTORY_PAGE_SIZE, q: query || undefined },
  })).data
}
