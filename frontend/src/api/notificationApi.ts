import type {
  NotificationResponse,
  NotificationUnreadCountResponse,
} from '../types/notification'
import { httpClient } from './httpClient'

export async function getMyNotifications(): Promise<NotificationResponse[]> {
  const response = await httpClient.get<NotificationResponse[]>(
    '/notifications/me',
  )
  return response.data
}

export async function getMyUnreadCount(): Promise<NotificationUnreadCountResponse> {
  const response = await httpClient.get<NotificationUnreadCountResponse>(
    '/notifications/me/unread-count',
  )
  return response.data
}

export async function markNotificationAsRead(
  notificationId: number,
): Promise<NotificationResponse> {
  const response = await httpClient.patch<NotificationResponse>(
    `/notifications/${notificationId}/read`,
  )
  return response.data
}

export async function markAllNotificationsAsRead(): Promise<void> {
  await httpClient.patch('/notifications/me/read-all')
}
