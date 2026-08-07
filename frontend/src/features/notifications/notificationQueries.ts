import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
  getMyNotifications,
  getMyUnreadCount,
  markAllNotificationsAsRead,
  markNotificationAsRead,
} from '../../api/notificationApi'

export const notificationQueryKeys = {
  root: ['notifications'] as const,
  list: (userId: number) =>
    [...notificationQueryKeys.root, userId, 'list'] as const,
  unreadCount: (userId: number) =>
    [...notificationQueryKeys.root, userId, 'unread-count'] as const,
}

export function useMyNotifications(userId: number) {
  return useQuery({
    queryKey: notificationQueryKeys.list(userId),
    queryFn: getMyNotifications,
  })
}

export function useMyUnreadCount(userId: number | null) {
  return useQuery({
    queryKey: notificationQueryKeys.unreadCount(userId ?? 0),
    queryFn: getMyUnreadCount,
    enabled: userId !== null,
    staleTime: 30_000,
  })
}

function useInvalidateNotificationState(userId: number) {
  const queryClient = useQueryClient()

  return async () => {
    await Promise.all([
      queryClient.invalidateQueries({
        queryKey: notificationQueryKeys.list(userId),
      }),
      queryClient.invalidateQueries({
        queryKey: notificationQueryKeys.unreadCount(userId),
      }),
    ])
  }
}

export function useMarkNotificationAsRead(userId: number) {
  const invalidateNotificationState = useInvalidateNotificationState(userId)

  return useMutation({
    mutationFn: markNotificationAsRead,
    onSuccess: invalidateNotificationState,
  })
}

export function useMarkAllNotificationsAsRead(userId: number) {
  const invalidateNotificationState = useInvalidateNotificationState(userId)

  return useMutation({
    mutationFn: markAllNotificationsAsRead,
    onSuccess: invalidateNotificationState,
  })
}
