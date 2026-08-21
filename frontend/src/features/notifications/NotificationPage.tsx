import { Bell, Check, CheckCheck, Inbox, LoaderCircle } from 'lucide-react'
import { useEffect, useState } from 'react'
import { toast } from 'sonner'
import { PageHeader } from '../../components/common/PageHeader'
import { Pagination } from '../../components/common/Pagination'
import type { NotificationResponse } from '../../types/notification'
import { getApiErrorMessage } from '../../utils/apiError'
import { formatDateTime } from '../../utils/formatDateTime'
import { useAuth } from '../auth/useAuth'
import {
  notificationPresentation,
  notificationToneClasses,
} from './notificationPresentation'
import {
  useMarkAllNotificationsAsRead,
  useMarkNotificationAsRead,
  useMyNotifications,
  useMyUnreadCount,
} from './notificationQueries'

type NotificationFilter = 'ALL' | 'UNREAD'

interface NotificationItemProps {
  notification: NotificationResponse
  isMarkingRead: boolean
  onMarkRead: (notificationId: number) => void
}

function NotificationItem({ notification, isMarkingRead, onMarkRead }: NotificationItemProps) {
  const presentation = notificationPresentation[notification.type]
  const toneClasses = notificationToneClasses[presentation.tone]
  const Icon = presentation.icon

  return (
    <article
      className={`border p-4 shadow-sm sm:p-5 ${
        notification.read
          ? 'border-slate-200 bg-white'
          : 'border-blue-200 bg-blue-50/40'
      }`}
    >
      <div className="flex items-start gap-3 sm:gap-4">
        <span
          className={`flex size-10 shrink-0 items-center justify-center ring-1 ${toneClasses.icon}`}
          aria-hidden="true"
        >
          <Icon size={20} />
        </span>

        <div className="min-w-0 flex-1">
          <div className="flex flex-col gap-2 sm:flex-row sm:items-start sm:justify-between">
            <div>
              <div className="flex flex-wrap items-center gap-2">
                <h2 className="text-base font-semibold text-slate-950">{notification.title}</h2>
                {!notification.read && (
                  <span className="inline-flex items-center gap-1 bg-blue-700 px-2 py-0.5 text-xs font-semibold text-white">
                    <span className="size-1.5 rounded-full bg-white" aria-hidden="true" />
                    Okunmadı
                  </span>
                )}
              </div>
              <span className={`mt-2 inline-flex px-2 py-1 text-xs font-medium ${toneClasses.label}`}>
                {presentation.label}
              </span>
            </div>
            <time
              className="shrink-0 text-xs font-medium text-slate-500 sm:text-right"
              dateTime={notification.createdAt}
            >
              {formatDateTime(notification.createdAt)}
            </time>
          </div>

          <p className="mt-3 whitespace-pre-wrap text-sm leading-6 text-slate-700">
            {notification.message}
          </p>

          <div className="mt-4 flex flex-wrap items-center justify-between gap-3 border-t border-slate-200/80 pt-3">
            <span className="inline-flex items-center gap-1.5 text-xs font-medium text-slate-500">
              {notification.read ? (
                <><Check aria-hidden="true" size={15} /> Okundu</>
              ) : (
                <><Bell aria-hidden="true" size={15} /> Yeni bildirim</>
              )}
            </span>

            {!notification.read && (
              <button
                type="button"
                onClick={() => onMarkRead(notification.id)}
                disabled={isMarkingRead}
                className="inline-flex min-h-9 items-center justify-center gap-2 border border-blue-700 px-3 py-1.5 text-sm font-semibold text-blue-800 hover:bg-blue-50 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-blue-700 disabled:cursor-not-allowed disabled:opacity-60"
                aria-label={`${notification.title} bildirimini okundu işaretle`}
              >
                {isMarkingRead ? (
                  <LoaderCircle className="animate-spin" aria-hidden="true" size={16} />
                ) : (
                  <Check aria-hidden="true" size={16} />
                )}
                Okundu işaretle
              </button>
            )}
          </div>
        </div>
      </div>
    </article>
  )
}

export function NotificationPage() {
  const { user } = useAuth()
  const userId = user?.userId ?? 0
  const [filter, setFilter] = useState<NotificationFilter>('ALL')
  const [page, setPage] = useState(0)
  const notificationsQuery = useMyNotifications(userId, page, filter === 'UNREAD')
  const unreadCountQuery = useMyUnreadCount(user?.userId ?? null)
  const markReadMutation = useMarkNotificationAsRead(userId)
  const markAllMutation = useMarkAllNotificationsAsRead(userId)

  const notifications = notificationsQuery.data?.content ?? []
  const unreadCount = unreadCountQuery.data?.unreadCount ?? 0
  const changeFilter = (nextFilter: NotificationFilter) => { setFilter(nextFilter); setPage(0) }

  useEffect(() => {
    if (notificationsQuery.data && page >= notificationsQuery.data.totalPages && notificationsQuery.data.totalPages > 0) {
      setPage(notificationsQuery.data.totalPages - 1)
    }
  }, [notificationsQuery.data, page])

  const handleMarkRead = (notificationId: number) => {
    markReadMutation.mutate(notificationId, {
      onSuccess: () => { if (filter === 'UNREAD') setPage(0); toast.success('Bildirim okundu olarak işaretlendi.') },
      onError: (error: unknown) => toast.error(
        getApiErrorMessage(error, 'Bildirim güncellenemedi. Lütfen tekrar deneyin.'),
      ),
    })
  }

  const handleMarkAllRead = () => {
    markAllMutation.mutate(undefined, {
      onSuccess: () => { setPage(0); toast.success('Tüm bildirimler okundu olarak işaretlendi.') },
      onError: (error: unknown) => toast.error(
        getApiErrorMessage(error, 'Bildirimler güncellenemedi. Lütfen tekrar deneyin.'),
      ),
    })
  }

  return (
    <section>
      <PageHeader
        title="Bildirimler"
        description="Hesabınıza ait sistem bildirimlerini görüntüleyebilir ve okunma durumlarını yönetebilirsiniz."
        actions={unreadCount > 0 ? (
          <button
            type="button"
            onClick={handleMarkAllRead}
            disabled={markAllMutation.isPending}
            className="inline-flex min-h-10 items-center justify-center gap-2 bg-blue-800 px-4 py-2 text-sm font-semibold text-white hover:bg-blue-900 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-blue-700 disabled:cursor-not-allowed disabled:opacity-60"
          >
            {markAllMutation.isPending ? (
              <LoaderCircle className="animate-spin" aria-hidden="true" size={17} />
            ) : (
              <CheckCheck aria-hidden="true" size={17} />
            )}
            Tümünü okundu işaretle
          </button>
        ) : null}
      />

      {!notificationsQuery.isLoading && !notificationsQuery.isError && (
        <div className="mt-6 flex flex-wrap items-center gap-2" aria-label="Bildirim filtreleri">
          {([
            ['ALL', 'Tümü'],
            ['UNREAD', `Okunmamış (${unreadCount})`],
          ] as const).map(([value, label]) => (
            <button
              key={value}
              type="button"
              onClick={() => changeFilter(value)}
              aria-pressed={filter === value}
              className={`min-h-9 border px-4 py-2 text-sm font-semibold focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-blue-700 ${
                filter === value
                  ? 'border-blue-800 bg-blue-800 text-white'
                  : 'border-slate-300 bg-white text-slate-700 hover:bg-slate-50'
              }`}
            >
              {label}
            </button>
          ))}
        </div>
      )}

      {notificationsQuery.isLoading && (
        <div className="mt-6 flex min-h-64 flex-col items-center justify-center border border-slate-200 bg-white px-6 text-center shadow-sm" role="status" aria-live="polite">
          <LoaderCircle className="animate-spin text-blue-700" aria-hidden="true" size={30} />
          <p className="mt-3 text-sm font-medium text-slate-700">Bildirimler yükleniyor...</p>
        </div>
      )}

      {notificationsQuery.isError && (
        <div className="mt-6 border border-red-200 bg-red-50 p-6 text-center" role="alert">
          <p className="font-semibold text-red-900">Bildirimler yüklenemedi</p>
          <p className="mt-2 text-sm leading-6 text-red-800">
            {getApiErrorMessage(notificationsQuery.error, 'Bildirimler alınırken bir sorun oluştu. Lütfen tekrar deneyin.')}
          </p>
          <button
            type="button"
            onClick={() => void notificationsQuery.refetch()}
            className="mt-4 min-h-10 border border-red-700 bg-white px-4 py-2 text-sm font-semibold text-red-800 hover:bg-red-100 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-red-700"
          >
            Tekrar dene
          </button>
        </div>
      )}

      {notificationsQuery.isSuccess && notifications.length === 0 && (
        <div className="mt-6 flex min-h-64 flex-col items-center justify-center border border-slate-200 bg-white px-6 text-center shadow-sm">
          <span className="flex size-12 items-center justify-center bg-slate-100 text-slate-500">
            <Inbox aria-hidden="true" size={25} />
          </span>
          <h2 className="mt-4 text-base font-semibold text-slate-900">
            {filter === 'UNREAD' ? 'Okunmamış bildiriminiz yok' : 'Henüz bildiriminiz yok'}
          </h2>
          <p className="mt-2 max-w-lg text-sm leading-6 text-slate-600">
            {filter === 'UNREAD'
              ? 'Tüm bildirimleriniz okunmuş durumda.'
              : 'Sistem bildirimleri oluştuğunda bu sayfada görüntülenecektir.'}
          </p>
        </div>
      )}

      {notificationsQuery.isSuccess && notifications.length > 0 && (
        <div className="mt-6" aria-live="polite">
          <div className="space-y-3">{notifications.map((notification) => (
            <NotificationItem
              key={notification.id}
              notification={notification}
              isMarkingRead={markReadMutation.isPending && markReadMutation.variables === notification.id}
              onMarkRead={handleMarkRead}
            />
          ))}</div>
          <Pagination currentPage={notificationsQuery.data.page} totalPages={notificationsQuery.data.totalPages} totalElements={notificationsQuery.data.totalElements} pageSize={notificationsQuery.data.size} onPageChange={setPage} itemLabel="bildirim" className="mt-5" />
        </div>
      )}
    </section>
  )
}
