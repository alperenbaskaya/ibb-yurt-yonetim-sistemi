import { ClipboardCheck, Clock3, Settings, UserRound } from 'lucide-react'
import { useEffect, useState } from 'react'
import { PageHeader } from '../../components/common/PageHeader'
import { Pagination } from '../../components/common/Pagination'
import { HistoryPersonSearch } from '../../components/common/HistoryPersonSearch'
import { useDebouncedValue } from '../../hooks/useDebouncedValue'
import type { Role } from '../../types/auth'
import type { AuditCategory, AuditLogPageResponse, AuditLogResponse } from '../../types/globalAdmin'
import { getApiErrorMessage } from '../../utils/apiError'
import { formatDateTime } from '../../utils/formatDateTime'
import { useAuth } from '../auth/useAuth'
import { AdminPageState } from '../dormitory-admin/AdminPageState'
import { useDormitories } from './globalAdminQueries'
import { useDormitoryOperationHistory, useSystemManagementHistory } from './globalHistoryQueries'

type MainView = 'DORMITORY' | 'SYSTEM'
type OperationCategory = Extract<AuditCategory, 'STUDENT_ACTIVITY' | 'REVIEWER_ACTIVITY'>

const roleLabels: Record<Role, string> = {
  ADMIN: 'Yönetici',
  REVIEWER: 'Değerlendirici',
  STUDENT: 'Öğrenci',
}

function TabButton({ selected, onClick, children }: { selected: boolean; onClick: () => void; children: string }) {
  return <button type="button" role="tab" aria-selected={selected} onClick={onClick} className={`min-h-11 border-b-2 px-4 py-2 text-sm font-semibold focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-blue-700 ${selected ? 'border-blue-700 text-blue-800' : 'border-transparent text-slate-600 hover:text-slate-950'}`}>{children}</button>
}

function HistoryCard({ event }: { event: AuditLogResponse }) {
  const Icon = event.category === 'REVIEWER_ACTIVITY' ? ClipboardCheck : event.category === 'SYSTEM_MANAGEMENT' ? Settings : UserRound
  return <li className="border border-slate-200 bg-white p-5 shadow-sm">
    <article className="flex gap-4">
      <span className="flex size-10 shrink-0 items-center justify-center bg-blue-50 text-blue-700"><Icon aria-hidden="true" size={20} /></span>
      <div className="min-w-0 flex-1">
        <div className="flex flex-col gap-1 sm:flex-row sm:items-start sm:justify-between">
          <p className="font-semibold leading-6 text-slate-950">{event.description}</p>
          <time className="flex shrink-0 items-center gap-1.5 text-sm text-slate-500" dateTime={event.createdAt}><Clock3 aria-hidden="true" size={15} />{formatDateTime(event.createdAt)}</time>
        </div>
        <dl className="mt-3 flex flex-wrap gap-x-6 gap-y-2 text-sm">
          {event.category === 'REVIEWER_ACTIVITY' && <div><dt className="inline text-slate-500">Değerlendirici: </dt><dd className="inline font-medium text-slate-800">{event.actorName}</dd></div>}
          {event.subjectStudentName && <div><dt className="inline text-slate-500">Öğrenci: </dt><dd className="inline font-medium text-slate-800">{event.subjectStudentName}</dd></div>}
          {event.category === 'SYSTEM_MANAGEMENT' && <div><dt className="inline text-slate-500">İşlemi yapan: </dt><dd className="inline font-medium text-slate-800">{event.actorName} · {roleLabels[event.actorRole]}</dd></div>}
          {event.category === 'STUDENT_ACTIVITY' && <div><dt className="inline text-slate-500">İşlemi yapan: </dt><dd className="inline font-medium text-slate-800">{event.actorName}</dd></div>}
          <div><dt className="inline text-slate-500">Hedef: </dt><dd className="inline font-medium text-slate-800">{event.targetLabel}</dd></div>
          {event.dormitoryName && event.category === 'SYSTEM_MANAGEMENT' && <div><dt className="inline text-slate-500">Yurt: </dt><dd className="inline font-medium text-slate-800">{event.dormitoryName}</dd></div>}
        </dl>
      </div>
    </article>
  </li>
}

function HistoryResults({ query, emptyTitle, emptyMessage, onPage }: { query: { isLoading: boolean; isError: boolean; error: unknown; data?: AuditLogPageResponse; refetch: () => Promise<unknown> }; emptyTitle: string; emptyMessage: string; onPage: (page: number) => void }) {
  if (query.isLoading) return <AdminPageState state="loading" title="İşlem geçmişi yükleniyor" message="En güncel kayıtlar alınıyor..." />
  if (query.isError) return <AdminPageState state="error" title="İşlem geçmişi alınamadı" message={getApiErrorMessage(query.error, 'İşlem geçmişi alınamadı.')} onRetry={() => void query.refetch()} />
  if (!query.data || query.data.items.length === 0) return <AdminPageState state="empty" title={emptyTitle} message={emptyMessage} />
  return <div className="mt-6"><p className="mb-3 text-sm text-slate-600">Toplam {query.data.totalElements} kayıt</p><ol className="space-y-3">{query.data.items.map((event) => <HistoryCard key={event.id} event={event} />)}</ol><Pagination currentPage={query.data.page} totalPages={query.data.totalPages} totalElements={query.data.totalElements} pageSize={query.data.size} onPageChange={onPage} itemLabel="kayıt" className="mt-5" /></div>
}

export function GlobalHistoryPage() {
  const { user } = useAuth()
  const userId = user?.userId ?? 0
  const [mainView, setMainView] = useState<MainView>('DORMITORY')
  const [dormitoryId, setDormitoryId] = useState<number | null>(null)
  const [category, setCategory] = useState<OperationCategory>('STUDENT_ACTIVITY')
  const [operationPage, setOperationPage] = useState(0)
  const [systemPage, setSystemPage] = useState(0)
  const [operationSearch, setOperationSearch] = useState('')
  const [systemSearch, setSystemSearch] = useState('')
  const debouncedOperationSearch = useDebouncedValue(operationSearch)
  const debouncedSystemSearch = useDebouncedValue(systemSearch)
  const dormitoriesQuery = useDormitories(userId)
  const activeDormitories = (dormitoriesQuery.data ?? []).filter((dormitory) => dormitory.active)
  const selectedDormitory = activeDormitories.find((dormitory) => dormitory.id === dormitoryId) ?? null
  const operationQuery = useDormitoryOperationHistory(userId, dormitoryId, category, operationPage, debouncedOperationSearch, mainView === 'DORMITORY')
  const systemQuery = useSystemManagementHistory(userId, systemPage, debouncedSystemSearch, mainView === 'SYSTEM')

  useEffect(() => {
    if (operationQuery.data && operationPage >= operationQuery.data.totalPages && operationQuery.data.totalPages > 0) {
      setOperationPage(operationQuery.data.totalPages - 1)
    }
  }, [operationPage, operationQuery.data])

  useEffect(() => {
    if (systemQuery.data && systemPage >= systemQuery.data.totalPages && systemQuery.data.totalPages > 0) {
      setSystemPage(systemQuery.data.totalPages - 1)
    }
  }, [systemPage, systemQuery.data])

  const changeMainView = (view: MainView) => { setMainView(view); setOperationPage(0); setSystemPage(0) }
  const changeDormitory = (value: string) => { setDormitoryId(value ? Number(value) : null); setOperationPage(0) }
  const changeCategory = (value: OperationCategory) => { setCategory(value); setOperationPage(0) }
  const changeOperationSearch = (value: string) => { setOperationSearch(value); setOperationPage(0) }
  const changeSystemSearch = (value: string) => { setSystemSearch(value); setSystemPage(0) }

  return <section>
    <PageHeader title="Sistem Geçmişi" description="Yurt operasyonlarını ve sistem yönetimi değişikliklerini kayıtlı tarihsel bilgilerle inceleyin." />
    <div className="mt-6 border-b border-slate-200" role="tablist" aria-label="Sistem geçmişi görünümü"><TabButton selected={mainView === 'DORMITORY'} onClick={() => changeMainView('DORMITORY')}>Yurt Operasyonları</TabButton><TabButton selected={mainView === 'SYSTEM'} onClick={() => changeMainView('SYSTEM')}>Sistem Yönetimi</TabButton></div>

    {mainView === 'DORMITORY' && <div role="tabpanel" className="pt-6">
      {dormitoriesQuery.isLoading && <AdminPageState state="loading" title="Yurtlar yükleniyor" message="Operasyon geçmişi için aktif yurtlar alınıyor..." />}
      {dormitoriesQuery.isError && <AdminPageState state="error" title="Yurtlar alınamadı" message={getApiErrorMessage(dormitoriesQuery.error, 'Yurt listesi alınamadı.')} onRetry={() => void dormitoriesQuery.refetch()} />}
      {dormitoriesQuery.isSuccess && activeDormitories.length === 0 && <AdminPageState state="empty" title="Aktif yurt bulunmuyor" message="Operasyon geçmişini filtrelemek için aktif bir yurt bulunmalıdır." />}
      {dormitoriesQuery.isSuccess && activeDormitories.length > 0 && <><label htmlFor="history-dormitory" className="block text-sm font-semibold text-slate-800">Yurt</label><select id="history-dormitory" value={dormitoryId ?? ''} onChange={(event) => changeDormitory(event.target.value)} className="mt-2 min-h-11 w-full max-w-xl border border-slate-300 bg-white px-3 py-2 text-sm focus:border-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-100"><option value="">Yurt seçiniz</option>{activeDormitories.map((dormitory) => <option key={dormitory.id} value={dormitory.id}>{dormitory.name}</option>)}</select></>}
      {dormitoriesQuery.isSuccess && activeDormitories.length > 0 && dormitoryId === null && <div className="mt-5 border border-slate-200 bg-white p-6 text-center text-sm text-slate-600">İşlem geçmişini görüntülemek için önce bir yurt seçin.</div>}
      {selectedDormitory && <><div className="mt-6 border border-slate-200 bg-slate-50 p-4"><p className="font-semibold text-slate-950">{selectedDormitory.name}</p><p className="mt-1 text-sm text-slate-600">Seçilen yurdun kayıtlı operasyon geçmişi</p></div><div className="mt-4 border-b border-slate-200" role="tablist" aria-label="Yurt operasyon kategorisi"><TabButton selected={category === 'STUDENT_ACTIVITY'} onClick={() => changeCategory('STUDENT_ACTIVITY')}>Öğrenci İşlemleri</TabButton><TabButton selected={category === 'REVIEWER_ACTIVITY'} onClick={() => changeCategory('REVIEWER_ACTIVITY')}>Değerlendirici İşlemleri</TabButton></div><HistoryPersonSearch value={operationSearch} onChange={changeOperationSearch} /><div role="tabpanel"><HistoryResults query={operationQuery} emptyTitle={operationSearch ? 'Aramanızla eşleşen geçmiş kaydı bulunamadı' : category === 'STUDENT_ACTIVITY' ? 'Öğrenci işlemi bulunmuyor' : 'Değerlendirici işlemi bulunmuyor'} emptyMessage={operationSearch ? 'Arama metnini değiştirerek tekrar deneyin.' : category === 'STUDENT_ACTIVITY' ? 'Bu yurt için kayıtlı öğrenci işlemi yok.' : 'Bu yurt için kayıtlı değerlendirici işlemi yok.'} onPage={setOperationPage} /></div></>}
    </div>}

    {mainView === 'SYSTEM' && <div role="tabpanel"><HistoryPersonSearch value={systemSearch} onChange={changeSystemSearch} /><HistoryResults query={systemQuery} emptyTitle={systemSearch ? 'Aramanızla eşleşen geçmiş kaydı bulunamadı' : 'Sistem yönetimi kaydı bulunmuyor'} emptyMessage={systemSearch ? 'Arama metnini değiştirerek tekrar deneyin.' : 'Henüz kayıtlı bir sistem yönetimi işlemi yok.'} onPage={setSystemPage} /></div>}
  </section>
}
