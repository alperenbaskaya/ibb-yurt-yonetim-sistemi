import { useState } from 'react'
import { Check, ChevronLeft, ChevronRight, LoaderCircle, X } from 'lucide-react'
import { toast } from 'sonner'
import { PageHeader } from '../../components/common/PageHeader'
import { useAuth } from '../auth/useAuth'
import type { AdmissionStatus } from '../../types/student'
import type { AdmissionResponse } from '../../types/globalAdmin'
import { formatDate } from '../../utils/formatDate'
import { getApiErrorMessage } from '../../utils/apiError'
import { AdminPageState } from '../dormitory-admin/AdminPageState'
import { AdminStatusBadge } from '../dormitory-admin/AdminStatusBadge'
import { adminAdmissionPresentation } from '../dormitory-admin/adminPresentation'
import { AdmissionApprovalDialog } from './AdmissionApprovalDialog'
import { useAdmissionMutations, useGlobalAdmissions } from './globalAdminQueries'

type Filter = 'ALL' | AdmissionStatus
type Dialog = 'selected' | 'all' | null
const filters: Filter[] = ['ALL', 'PENDING', 'APPROVED', 'REJECTED']

export function GlobalAdmissionsPage() {
  const { user } = useAuth()
  const userId = user?.userId ?? 0
  const [filter, setFilter] = useState<Filter>('ALL')
  const [page, setPage] = useState(0)
  const [selectedIds, setSelectedIds] = useState<Set<number>>(new Set())
  const [dialog, setDialog] = useState<Dialog>(null)
  const [dialogError, setDialogError] = useState<string | null>(null)
  const query = useGlobalAdmissions(userId, page, filter === 'ALL' ? undefined : filter)
  const mutations = useAdmissionMutations(userId)
  const bulkPending = mutations.selected.isPending || mutations.all.isPending
  const pendingOnPage = query.data?.content.filter(admission => admission.status === 'PENDING') ?? []
  const selectedAdmissions = pendingOnPage.filter(admission => selectedIds.has(admission.id))
  const allPagePendingSelected = pendingOnPage.length > 0 && pendingOnPage.every(admission => selectedIds.has(admission.id))

  const clearSelection = () => setSelectedIds(new Set())
  const changeFilter = (nextFilter: Filter) => { setFilter(nextFilter); setPage(0); clearSelection() }
  const changePage = (nextPage: number) => { setPage(nextPage); clearSelection() }
  const toggleAdmission = (id: number) => setSelectedIds(current => { const next = new Set(current); if (next.has(id)) next.delete(id); else next.add(id); return next })
  const togglePage = () => setSelectedIds(allPagePendingSelected ? new Set() : new Set(pendingOnPage.map(admission => admission.id)))

  const decide = (admission: AdmissionResponse, status: 'APPROVED' | 'REJECTED') => {
    const name = `${admission.studentFirstName} ${admission.studentLastName}`
    if (!window.confirm(`${name} kabul kaydı ${status === 'APPROVED' ? 'onaylansın' : 'reddedilsin'} mı? Karar geri alınamaz.`)) return
    mutations.single.mutate({ id: admission.id, status }, { onSuccess: () => { clearSelection(); toast.success('Kabul durumu güncellendi.') }, onError: error => toast.error(getApiErrorMessage(error, 'Kabul güncellenemedi.')) })
  }

  const approveSelected = async () => {
    setDialogError(null)
    try { const result = await mutations.selected.mutateAsync({ admissionIds: selectedAdmissions.map(admission => admission.id) }); clearSelection(); setDialog(null); toast.success(`${result.approvedCount} öğrenci onaylandı.`) }
    catch (error) { setDialogError(getApiErrorMessage(error, 'Seçilen öğrenciler onaylanamadı.')) }
  }
  const approveAll = async () => {
    setDialogError(null)
    try { const result = await mutations.all.mutateAsync(); clearSelection(); setDialog(null); toast.success(`${result.approvedCount} bekleyen öğrenci onaylandı.`) }
    catch (error) { setDialogError(getApiErrorMessage(error, 'Bekleyen öğrenciler onaylanamadı.')) }
  }

  return <section>
    <PageHeader title="Kabuller" description="Aktif dönemde tüm yurtlara ait kabul kayıtlarını izleyin ve bekleyen kayıtları karara bağlayın."/>
    <div className="mt-6 flex flex-wrap gap-2">{filters.map(item => <button key={item} type="button" onClick={() => changeFilter(item)} className={`border px-3 py-2 text-sm ${filter === item ? 'bg-blue-800 text-white' : 'bg-white text-slate-700'}`}>{item === 'ALL' ? 'Tümü' : adminAdmissionPresentation[item].label}</button>)}</div>
    {filter === 'PENDING' && query.isSuccess && <div className="mt-4 flex flex-wrap items-center justify-between gap-3 border border-amber-200 bg-amber-50 p-4"><div><p className="font-semibold text-slate-900">Aktif dönemde {query.data.totalElements} bekleyen öğrenci</p><p className="text-sm text-slate-600">Bu işlem yalnızca aktif dönemdeki bekleyen kayıtları kapsar.</p></div><button type="button" disabled={query.data.totalElements === 0 || bulkPending || mutations.single.isPending} onClick={() => { setDialogError(null); setDialog('all') }} className="border border-emerald-700 bg-white px-4 py-2 text-sm font-semibold text-emerald-800 disabled:opacity-50">Tüm Bekleyenleri Onayla</button></div>}
    {query.isLoading && <AdminPageState state="loading" title="Kabuller yükleniyor" message="Global kabul kayıtları alınıyor..."/>}
    {query.isError && <AdminPageState state="error" title="Kabuller alınamadı" message={getApiErrorMessage(query.error, 'Kabuller alınamadı.')} onRetry={() => void query.refetch()}/>}
    {query.isSuccess && query.data.content.length === 0 && <AdminPageState state="empty" title="Kabul yok" message="Bu filtrede aktif döneme ait kabul kaydı bulunmuyor."/>}
    {query.isSuccess && query.data.content.length > 0 && <div className="mt-4">
      {pendingOnPage.length > 0 && <div className="mb-4 flex flex-wrap items-center gap-4 border border-slate-200 bg-white p-3"><label className="flex cursor-pointer items-center gap-2 text-sm font-medium"><input type="checkbox" checked={allPagePendingSelected} onChange={togglePage} disabled={bulkPending} className="h-4 w-4"/>Bu sayfadaki bekleyenleri seç</label><span className="text-sm text-slate-600">{selectedIds.size} öğrenci seçildi</span><button type="button" disabled={selectedIds.size === 0 || bulkPending || mutations.single.isPending} onClick={() => { setDialogError(null); setDialog('selected') }} className="ml-auto bg-emerald-700 px-4 py-2 text-sm font-semibold text-white disabled:opacity-50">Seçilenleri Onayla</button></div>}
      <div className="grid gap-3 lg:grid-cols-2">{query.data.content.map(admission => { const presentation = adminAdmissionPresentation[admission.status]; const rowPending = mutations.single.isPending && mutations.single.variables?.id === admission.id; const name = `${admission.studentFirstName} ${admission.studentLastName}`; return <article key={admission.id} className="border border-slate-200 bg-white p-4">
        <div className="flex justify-between gap-2"><div className="flex items-start gap-3">{admission.status === 'PENDING' && <input aria-label={`${name} öğrencisini seç`} type="checkbox" checked={selectedIds.has(admission.id)} onChange={() => toggleAdmission(admission.id)} disabled={bulkPending} className="mt-1 h-4 w-4"/>}<div><h2 className="font-semibold">{name}</h2><p className="text-sm text-slate-600">{admission.dormitoryName} · {admission.dormitoryTermName}</p></div></div><AdminStatusBadge label={presentation.label} tone={presentation.tone}/></div>
        <p className="mt-3 text-sm">Kabul tarihi: {formatDate(admission.admissionDate)} · Kimlik: {admission.identityNumber}</p>
        {admission.status === 'PENDING' && <div className="mt-4 flex gap-2"><button disabled={bulkPending || mutations.single.isPending} onClick={() => decide(admission, 'APPROVED')} className="bg-emerald-700 px-3 py-2 text-sm text-white disabled:opacity-50">{rowPending ? <LoaderCircle className="inline animate-spin" size={16}/> : <Check className="inline" size={16}/>} Onayla</button><button disabled={bulkPending || mutations.single.isPending} onClick={() => decide(admission, 'REJECTED')} className="border border-red-700 px-3 py-2 text-sm text-red-700 disabled:opacity-50"><X className="inline" size={16}/> Reddet</button></div>}
      </article>})}</div>
      <div className="mt-5 flex items-center justify-between border-t border-slate-200 pt-4"><button type="button" disabled={query.data.first || bulkPending} onClick={() => changePage(page - 1)} className="inline-flex items-center gap-1 border bg-white px-3 py-2 text-sm disabled:opacity-50"><ChevronLeft size={16}/>Önceki</button><p className="text-sm font-medium text-slate-600">Sayfa {query.data.page + 1} / {query.data.totalPages}</p><button type="button" disabled={query.data.last || bulkPending} onClick={() => changePage(page + 1)} className="inline-flex items-center gap-1 border bg-white px-3 py-2 text-sm disabled:opacity-50">Sonraki<ChevronRight size={16}/></button></div>
    </div>}
    {dialog === 'selected' && <AdmissionApprovalDialog title="Seçilen öğrencileri onaylamak istediğinize emin misiniz?" description={`${selectedAdmissions.length} öğrencinin kabul kaydı onaylanacaktır.`} names={selectedAdmissions.map(admission => `${admission.studentFirstName} ${admission.studentLastName}`)} confirmLabel={`${selectedAdmissions.length} Öğrenciyi Onayla`} pending={mutations.selected.isPending} errorMessage={dialogError} onCancel={() => setDialog(null)} onConfirm={() => void approveSelected()}/>}
    {dialog === 'all' && query.data && <AdmissionApprovalDialog title={`${query.data.totalElements} bekleyen öğrencinin tamamını onaylamak istediğinize emin misiniz?`} description={`Bu işlem aktif dönemdeki ${query.data.totalElements} kabul kaydını onaylayacaktır.`} confirmLabel={`${query.data.totalElements} Öğrencinin Tümünü Onayla`} pending={mutations.all.isPending} errorMessage={dialogError} onCancel={() => setDialog(null)} onConfirm={() => void approveAll()}/>}
  </section>
}
