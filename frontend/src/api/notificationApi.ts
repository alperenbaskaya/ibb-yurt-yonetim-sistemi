import type {
  NotificationResponse,
  NotificationUnreadCountResponse,
  NotificationPageResponse,
} from '../types/notification'
import { httpClient } from './httpClient'

export async function getMyNotifications(page: number, unreadOnly: boolean): Promise<NotificationPageResponse> {
  const response = await httpClient.get<NotificationPageResponse>(
    unreadOnly ? '/notifications/me/unread' : '/notifications/me',
    { params: { page, size: 10 } },
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
