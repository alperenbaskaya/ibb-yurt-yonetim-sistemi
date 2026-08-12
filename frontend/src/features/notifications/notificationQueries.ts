import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
  getMyNotifications,
  getMyUnreadCount,
  markAllNotificationsAsRead,
  markNotificationAsRead,
} from '../../api/notificationApi'

export const notificationQueryKeys = {
  root: ['notifications'] as const,
  lists: (userId: number) =>
    [...notificationQueryKeys.root, userId, 'list'] as const,
  list: (userId: number, page: number, unreadOnly: boolean) =>
    [...notificationQueryKeys.lists(userId), page, unreadOnly ? 'UNREAD' : 'ALL'] as const,
  unreadCount: (userId: number) =>
    [...notificationQueryKeys.root, userId, 'unread-count'] as const,
}

export function useMyNotifications(userId: number, page: number, unreadOnly: boolean) {
  return useQuery({
    queryKey: notificationQueryKeys.list(userId, page, unreadOnly),
    queryFn: () => getMyNotifications(page, unreadOnly),
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
        queryKey: notificationQueryKeys.lists(userId),
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
