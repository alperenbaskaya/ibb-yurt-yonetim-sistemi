import { AlertCircle, Inbox, LoaderCircle, X } from 'lucide-react'
import { useEffect, useRef } from 'react'
import type { StudentProcessTimelineItemResponse } from '../../types/student'
import { getApiErrorMessage } from '../../utils/apiError'
import { StudentProcessTimeline } from './StudentProcessTimeline'

interface StudentDocumentHistoryDialogProps {
  documentTypeId: number
  documentTypeName: string
  isLoading: boolean
  error: unknown
  items: StudentProcessTimelineItemResponse[]
  onRetry: () => void
  onClose: () => void
}

export function StudentDocumentHistoryDialog({ documentTypeId, documentTypeName, isLoading, error, items, onRetry, onClose }: StudentDocumentHistoryDialogProps) {
  const closeRef = useRef<HTMLButtonElement>(null)
  const documentItems = items.filter((item) => item.documentTypeId === documentTypeId)

  useEffect(() => {
    closeRef.current?.focus()
    const onKeyDown = (event: KeyboardEvent) => { if (event.key === 'Escape') onClose() }
    window.addEventListener('keydown', onKeyDown)
    return () => window.removeEventListener('keydown', onKeyDown)
  }, [onClose])

  return <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-950/40 p-3 sm:p-6" onMouseDown={(event) => { if (event.target === event.currentTarget) onClose() }}>
    <section role="dialog" aria-modal="true" aria-labelledby="student-document-history-title" className="flex max-h-[90vh] w-full max-w-2xl flex-col border border-slate-200 bg-white shadow-2xl">
      <header className="flex shrink-0 items-start justify-between gap-4 border-b border-slate-200 px-5 py-4"><div><h2 id="student-document-history-title" className="text-lg font-semibold text-slate-950">{documentTypeName} Geçmişi</h2><p className="mt-1 text-sm text-slate-600">Aktif dönemdeki yükleme ve değerlendirme adımları</p></div><button ref={closeRef} type="button" onClick={onClose} aria-label="Belge geçmişini kapat" className="flex size-10 shrink-0 items-center justify-center text-slate-500 hover:bg-slate-100 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-blue-700"><X aria-hidden="true" size={21} /></button></header>
      <div className="min-h-48 overflow-y-auto p-5 sm:p-6">
        {isLoading && <div className="flex min-h-40 flex-col items-center justify-center" role="status"><LoaderCircle aria-hidden="true" className="animate-spin text-blue-700" /><p className="mt-3 text-sm text-slate-600">Belge geçmişi yükleniyor...</p></div>}
        {Boolean(error) && !isLoading && <div className="flex min-h-40 flex-col items-center justify-center text-center" role="alert"><AlertCircle aria-hidden="true" className="text-red-700" /><p className="mt-3 font-semibold text-red-900">Belge geçmişi alınamadı</p><p className="mt-1 text-sm text-red-800">{getApiErrorMessage(error, 'Belge geçmişi alınamadı.')}</p><button type="button" onClick={onRetry} className="mt-4 min-h-10 border border-red-700 px-4 py-2 text-sm font-semibold text-red-800 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-red-700">Tekrar dene</button></div>}
        {!isLoading && !error && documentItems.length === 0 && <div className="flex min-h-40 flex-col items-center justify-center text-center" role="status"><Inbox aria-hidden="true" className="text-slate-500" /><p className="mt-3 font-semibold text-slate-900">Geçmiş kaydı bulunmuyor</p><p className="mt-1 text-sm text-slate-600">Bu belge için aktif dönemde kayıtlı bir işlem yok.</p></div>}
        {!isLoading && !error && documentItems.length > 0 && <StudentProcessTimeline items={documentItems} />}
      </div>
    </section>
  </div>
}
