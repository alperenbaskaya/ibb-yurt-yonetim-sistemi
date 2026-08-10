import { Activity, AlertCircle, Inbox, LoaderCircle, X } from 'lucide-react'
import { useEffect, useRef, useState } from 'react'
import type { AuditAction } from '../../types/globalAdmin'
import { getApiErrorMessage } from '../../utils/apiError'
import { formatDateTime } from '../../utils/formatDateTime'
import { useAuth } from '../auth/useAuth'
import { useRecentReviewerActivity } from './reviewerActivityQueries'

const actionLabels: Record<Extract<AuditAction, 'DOCUMENT_UPLOADED' | 'DOCUMENT_REUPLOADED'>, string> = {
  DOCUMENT_UPLOADED: 'Belge yükledi',
  DOCUMENT_REUPLOADED: 'Belgeyi yeniden yükledi',
}

export function ReviewerActivityFeed() {
  const { user } = useAuth()
  const [open, setOpen] = useState(false)
  const closeButtonRef = useRef<HTMLButtonElement>(null)
  const triggerRef = useRef<HTMLButtonElement>(null)
  const activityQuery = useRecentReviewerActivity(user?.userId ?? 0, open)

  useEffect(() => {
    if (!open) return
    closeButtonRef.current?.focus()
    const closeOnEscape = (event: KeyboardEvent) => {
      if (event.key === 'Escape') {
        setOpen(false)
        triggerRef.current?.focus()
      }
    }
    window.addEventListener('keydown', closeOnEscape)
    return () => window.removeEventListener('keydown', closeOnEscape)
  }, [open])

  const close = () => {
    setOpen(false)
    window.setTimeout(() => triggerRef.current?.focus(), 0)
  }

  return <>
    <button ref={triggerRef} type="button" onClick={() => setOpen(true)} className="fixed bottom-5 right-4 z-30 flex min-h-11 items-center gap-2 border border-blue-800 bg-blue-800 px-4 py-2.5 text-sm font-semibold text-white shadow-lg hover:bg-blue-900 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-blue-700 sm:bottom-6 sm:right-6" aria-haspopup="dialog" aria-expanded={open}><Activity aria-hidden="true" size={18} />Son Akış</button>

    {open && <div className="fixed inset-0 z-50 bg-slate-950/35 sm:flex sm:items-end sm:justify-end sm:p-6" onMouseDown={(event) => { if (event.target === event.currentTarget) close() }}>
      <section role="dialog" aria-modal="true" aria-labelledby="reviewer-activity-title" className="absolute inset-x-0 bottom-0 flex max-h-[88vh] flex-col border border-slate-200 bg-white shadow-2xl sm:static sm:max-h-[min(38rem,calc(100vh-3rem))] sm:w-[25rem]">
        <header className="flex shrink-0 items-center justify-between border-b border-slate-200 px-5 py-4">
          <div><h2 id="reviewer-activity-title" className="text-lg font-semibold text-slate-950">Son Akış</h2><p className="mt-1 text-sm text-slate-500">Yurdunuzdaki son belge yüklemeleri</p></div>
          <button ref={closeButtonRef} type="button" onClick={close} aria-label="Son Akış panelini kapat" className="flex size-10 items-center justify-center text-slate-500 hover:bg-slate-100 hover:text-slate-900 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-blue-700"><X aria-hidden="true" size={21} /></button>
        </header>
        <div className="min-h-48 overflow-y-auto p-4" aria-live="polite">
          {activityQuery.isLoading && <div className="flex min-h-40 flex-col items-center justify-center text-center" role="status"><LoaderCircle aria-hidden="true" className="animate-spin text-blue-700" size={27} /><p className="mt-3 text-sm text-slate-600">Son işlemler yükleniyor...</p></div>}
          {activityQuery.isError && <div className="flex min-h-40 flex-col items-center justify-center text-center" role="alert"><AlertCircle aria-hidden="true" className="text-red-700" size={27} /><p className="mt-3 font-semibold text-red-900">Son akış alınamadı</p><p className="mt-1 text-sm text-red-800">{getApiErrorMessage(activityQuery.error, 'Son belge hareketleri alınamadı.')}</p><button type="button" onClick={() => void activityQuery.refetch()} className="mt-4 min-h-10 border border-red-700 px-4 py-2 text-sm font-semibold text-red-800 hover:bg-red-50 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-red-700">Tekrar dene</button></div>}
          {activityQuery.isSuccess && activityQuery.data.length === 0 && <div className="flex min-h-40 flex-col items-center justify-center text-center" role="status"><Inbox aria-hidden="true" className="text-slate-500" size={27} /><p className="mt-3 font-semibold text-slate-900">Henüz belge hareketi yok</p><p className="mt-1 text-sm text-slate-600">Yurdunuzdaki yeni yüklemeler burada görünecek.</p></div>}
          {activityQuery.isSuccess && activityQuery.data.length > 0 && <ol className="divide-y divide-slate-200">{activityQuery.data.map((event) => {
            const action = event.action as 'DOCUMENT_UPLOADED' | 'DOCUMENT_REUPLOADED'
            return <li key={event.id} className="py-4 first:pt-1 last:pb-1"><p className="font-semibold text-slate-950">{event.subjectStudentName ?? event.actorName}</p><p className="mt-1 text-sm text-slate-700">{event.targetLabel}</p><p className="mt-1 text-sm font-medium text-blue-800">{actionLabels[action]}</p><time dateTime={event.createdAt} className="mt-2 block text-xs text-slate-500">{formatDateTime(event.createdAt)}</time></li>
          })}</ol>}
        </div>
      </section>
    </div>}
  </>
}
