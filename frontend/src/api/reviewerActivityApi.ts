import type { AuditLogResponse } from '../types/globalAdmin'
import { httpClient } from './httpClient'

export async function getRecentReviewerActivity(): Promise<AuditLogResponse[]> {
  return (await httpClient.get<AuditLogResponse[]>('/reviewer-activity/me/recent')).data
}
