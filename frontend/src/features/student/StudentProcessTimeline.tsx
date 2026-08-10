import { CheckCircle2, Circle, FileClock, MessageSquareWarning } from 'lucide-react'
import type { StudentProcessTimelineItemResponse } from '../../types/student'
import { formatDateTime } from '../../utils/formatDateTime'

function eventTone(action: StudentProcessTimelineItemResponse['action']): string {
  if (action === 'DOCUMENT_PROCESS_COMPLETED' || action === 'DOCUMENT_APPROVED' || action === 'ADMISSION_APPROVED') return 'border-emerald-600 bg-emerald-50 text-emerald-700'
  if (action === 'DOCUMENT_REJECTED' || action === 'ADMISSION_REJECTED') return 'border-red-600 bg-red-50 text-red-700'
  if (action === 'DOCUMENT_REVISION_REQUIRED') return 'border-amber-600 bg-amber-50 text-amber-700'
  return 'border-blue-600 bg-blue-50 text-blue-700'
}

export function StudentProcessTimeline({ items, compact = false }: { items: StudentProcessTimelineItemResponse[]; compact?: boolean }) {
  const visibleItems = compact && items.length > 8 ? items.slice(-8) : items
  return <ol className="relative ml-2 border-l border-slate-300">
    {visibleItems.map((item) => {
      const Icon = item.action === 'DOCUMENT_PROCESS_COMPLETED' || item.action === 'DOCUMENT_APPROVED' || item.action === 'ADMISSION_APPROVED' ? CheckCircle2 : item.action === 'DOCUMENT_REVISION_REQUIRED' || item.action === 'DOCUMENT_REJECTED' ? MessageSquareWarning : item.action === 'DOCUMENT_UPLOADED' || item.action === 'DOCUMENT_REUPLOADED' ? FileClock : Circle
      const showComment = (item.reviewDecision === 'REVISION_REQUIRED' || item.reviewDecision === 'REJECTED') && item.reviewComment
      return <li key={item.key} className="relative pb-6 pl-7 last:pb-0">
        <span className={`absolute -left-[13px] top-0 flex size-6 items-center justify-center rounded-full border ${eventTone(item.action)}`}><Icon aria-hidden="true" size={14} /></span>
        <p className="font-semibold leading-6 text-slate-900">{item.description}</p>
        {showComment && <blockquote className="mt-2 border-l-2 border-slate-300 pl-3 text-sm italic leading-6 text-slate-600">“{item.reviewComment}”</blockquote>}
        <time dateTime={item.createdAt} className="mt-2 block text-xs font-medium text-slate-500">{formatDateTime(item.createdAt)}</time>
      </li>
    })}
  </ol>
}
