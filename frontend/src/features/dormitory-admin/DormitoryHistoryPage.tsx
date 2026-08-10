import { ClipboardCheck, Clock3, UserRound } from 'lucide-react'
import { useState } from 'react'
import { PageHeader } from '../../components/common/PageHeader'
import type { DormitoryHistoryCategory } from '../../api/dormitoryAdminHistoryApi'
import type { AuditLogPageResponse, AuditLogResponse } from '../../types/globalAdmin'
import { getApiErrorMessage } from '../../utils/apiError'
import { formatDateTime } from '../../utils/formatDateTime'
import { useAuth } from '../auth/useAuth'
import { AdminPageState } from './AdminPageState'
import { useDormitoryAdminHistory } from './dormitoryAdminHistoryQueries'

function HistoryTab({ selected, onClick, children }: { selected: boolean; onClick: () => void; children: string }) {
  return <button type="button" role="tab" aria-selected={selected} onClick={onClick} className={`min-h-11 border-b-2 px-4 py-2 text-sm font-semibold focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-blue-700 ${selected ? 'border-blue-700 text-blue-800' : 'border-transparent text-slate-600 hover:text-slate-950'}`}>{children}</button>
}

function HistoryPagination({ data, onPage }: { data: AuditLogPageResponse; onPage: (page: number) => void }) {
  return <nav className="mt-5 flex items-center justify-between border-t border-slate-200 pt-4" aria-label="İşlem geçmişi sayfaları">
    <button type="button" disabled={data.first} onClick={() => onPage(data.page - 1)} aria-label="Önceki işlem geçmişi sayfası" className="min-h-10 border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-700 hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-45 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-blue-700">Önceki</button>
    <p className="text-sm font-medium text-slate-600">Sayfa {data.page + 1} / {data.totalPages}</p>
    <button type="button" disabled={data.last} onClick={() => onPage(data.page + 1)} aria-label="Sonraki işlem geçmişi sayfası" className="min-h-10 border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-700 hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-45 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-blue-700">Sonraki</button>
  </nav>
}

function DormitoryHistoryCard({ event }: { event: AuditLogResponse }) {
  const reviewerEvent = event.category === 'REVIEWER_ACTIVITY'
  const Icon = reviewerEvent ? ClipboardCheck : UserRound

  return <li className="border border-slate-200 bg-white p-5 shadow-sm">
    <article className="flex gap-4">
      <span className="flex size-10 shrink-0 items-center justify-center bg-blue-50 text-blue-700"><Icon aria-hidden="true" size={20} /></span>
      <div className="min-w-0 flex-1">
        <div className="flex flex-col gap-1 sm:flex-row sm:items-start sm:justify-between">
          <div>
            <h2 className="font-semibold text-slate-950">{reviewerEvent ? event.actorName : event.subjectStudentName ?? event.targetLabel}</h2>
            {reviewerEvent && <p className="mt-0.5 text-sm text-slate-500">Değerlendirici</p>}
          </div>
          <time className="flex shrink-0 items-center gap-1.5 text-sm text-slate-500" dateTime={event.createdAt}><Clock3 aria-hidden="true" size={15} />{formatDateTime(event.createdAt)}</time>
        </div>
        <p className="mt-3 leading-6 text-slate-800">{event.description}</p>
        <dl className="mt-3 flex flex-wrap gap-x-6 gap-y-2 text-sm">
          {reviewerEvent && event.subjectStudentName && <div><dt className="inline text-slate-500">Öğrenci: </dt><dd className="inline font-medium text-slate-800">{event.subjectStudentName}</dd></div>}
          {reviewerEvent && <div><dt className="inline text-slate-500">Belge: </dt><dd className="inline font-medium text-slate-800">{event.targetLabel}</dd></div>}
          {!reviewerEvent && <div><dt className="inline text-slate-500">İşlemi yapan: </dt><dd className="inline font-medium text-slate-800">{event.actorName}</dd></div>}
          {!reviewerEvent && event.targetLabel && <div><dt className="inline text-slate-500">Hedef: </dt><dd className="inline font-medium text-slate-800">{event.targetLabel}</dd></div>}
        </dl>
      </div>
    </article>
  </li>
}

export function DormitoryHistoryPage() {
  const { user } = useAuth()
  const userId = user?.userId ?? 0
  const [category, setCategory] = useState<DormitoryHistoryCategory>('STUDENT_ACTIVITY')
  const [page, setPage] = useState(0)
  const historyQuery = useDormitoryAdminHistory(userId, category, page)

  const changeCategory = (nextCategory: DormitoryHistoryCategory) => {
    setCategory(nextCategory)
    setPage(0)
  }

  const emptyStudent = category === 'STUDENT_ACTIVITY'

  return <section>
    <PageHeader title="İşlem Geçmişi" description="Yurdunuzdaki öğrenci ve değerlendirici işlemlerini kayıtlı tarihsel bilgilerle inceleyin." />
    {user?.dormitoryName && <div className="mt-6 border border-slate-200 bg-slate-50 p-4"><p className="text-sm text-slate-500">Yurt</p><p className="mt-1 font-semibold text-slate-950">{user.dormitoryName}</p></div>}
    <div className="mt-6 border-b border-slate-200" role="tablist" aria-label="İşlem geçmişi kategorisi"><HistoryTab selected={category === 'STUDENT_ACTIVITY'} onClick={() => changeCategory('STUDENT_ACTIVITY')}>Öğrenci İşlemleri</HistoryTab><HistoryTab selected={category === 'REVIEWER_ACTIVITY'} onClick={() => changeCategory('REVIEWER_ACTIVITY')}>Değerlendirici İşlemleri</HistoryTab></div>
    <div role="tabpanel">
      {historyQuery.isLoading && <AdminPageState state="loading" title="İşlem geçmişi yükleniyor" message="Yurdunuzun en güncel işlem kayıtları alınıyor..." />}
      {historyQuery.isError && <AdminPageState state="error" title="İşlem geçmişi alınamadı" message={getApiErrorMessage(historyQuery.error, 'İşlem geçmişi alınamadı.')} onRetry={() => void historyQuery.refetch()} />}
      {historyQuery.isSuccess && historyQuery.data.items.length === 0 && <AdminPageState state="empty" title={emptyStudent ? 'Öğrenci işlem geçmişi bulunmuyor' : 'Değerlendirici işlem geçmişi bulunmuyor'} message={emptyStudent ? 'Bu yurtta henüz öğrenci işlem geçmişi bulunmuyor.' : 'Bu yurtta henüz değerlendirici işlem geçmişi bulunmuyor.'} />}
      {historyQuery.isSuccess && historyQuery.data.items.length > 0 && <div className="mt-6"><p className="mb-3 text-sm text-slate-600">Toplam {historyQuery.data.totalElements} kayıt</p><ol className="space-y-3">{historyQuery.data.items.map((event) => <DormitoryHistoryCard key={event.id} event={event} />)}</ol><HistoryPagination data={historyQuery.data} onPage={setPage} /></div>}
    </div>
  </section>
}
