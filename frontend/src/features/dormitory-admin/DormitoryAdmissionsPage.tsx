import { Check, LoaderCircle, X } from 'lucide-react'
import { useState } from 'react'
import { toast } from 'sonner'
import { PageHeader } from '../../components/common/PageHeader'
import type { AdmissionStatus } from '../../types/student'
import { getApiErrorMessage } from '../../utils/apiError'
import { formatDate } from '../../utils/formatDate'
import { useAuth } from '../auth/useAuth'
import { AdminPageState } from './AdminPageState'
import { AdminStatusBadge } from './AdminStatusBadge'
import { adminAdmissionPresentation } from './adminPresentation'
import { useDormitoryAdmissions, useUpdateAdmissionStatus } from './dormitoryAdminQueries'

export function DormitoryAdmissionsPage() {
  const { user } = useAuth()
  const userId = user?.userId ?? 0
  const [filter, setFilter] = useState<'ALL' | AdmissionStatus>('ALL')
  const admissionsQuery = useDormitoryAdmissions(userId)
  const updateMutation = useUpdateAdmissionStatus(userId)
  const admissions = admissionsQuery.data ?? []
  const visible = filter === 'ALL' ? admissions : admissions.filter((item) => item.status === filter)

  const decide = (admissionId: number, status: 'APPROVED' | 'REJECTED', studentName: string) => {
    const action = status === 'APPROVED' ? 'onaylamak' : 'reddetmek'
    if (!window.confirm(`${studentName} öğrencisinin kabul kaydını ${action} istediğinizden emin misiniz? Bu karar geri alınamaz.`)) return
    updateMutation.mutate({ admissionId, request: { status } }, {
      onSuccess: () => toast.success(status === 'APPROVED' ? 'Kabul kaydı onaylandı.' : 'Kabul kaydı reddedildi.'),
      onError: (error: unknown) => toast.error(getApiErrorMessage(error, 'Kabul durumu güncellenemedi.')),
    })
  }

  return <section><PageHeader title="Kabuller" description="Aktif dönemde yurdunuza ait kabul kayıtlarını inceleyin ve bekleyen kayıtları karara bağlayın." />
    {admissionsQuery.isLoading && <AdminPageState state="loading" title="Kabuller yükleniyor" message="Aktif dönem kabul kayıtları alınıyor..." />}
    {admissionsQuery.isError && <AdminPageState state="error" title="Kabuller yüklenemedi" message={getApiErrorMessage(admissionsQuery.error, 'Kabul kayıtları alınamadı.')} onRetry={() => void admissionsQuery.refetch()} />}
    {admissionsQuery.isSuccess && admissions.length === 0 && <AdminPageState state="empty" title="Kabul kaydı yok" message="Yurdunuz için aktif dönemde kabul kaydı bulunmuyor." />}
    {admissionsQuery.isSuccess && admissions.length > 0 && <div className="mt-6 space-y-5">
      <div className="flex flex-wrap gap-2" aria-label="Kabul durumu filtreleri">{([['ALL','Tümü'],['PENDING','Bekliyor'],['APPROVED','Onaylandı'],['REJECTED','Reddedildi']] as const).map(([value,label]) => <button key={value} type="button" onClick={() => setFilter(value)} aria-pressed={filter === value} className={`min-h-9 border px-3 py-2 text-sm font-semibold focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-blue-700 ${filter === value ? 'border-blue-800 bg-blue-800 text-white' : 'border-slate-300 bg-white text-slate-700 hover:bg-slate-50'}`}>{label}</button>)}</div>
      {visible.length === 0 ? <div className="border border-slate-200 bg-white p-6 text-center text-sm text-slate-600">Seçilen durumda kabul kaydı bulunmuyor.</div> : <div className="grid gap-4 lg:grid-cols-2">{visible.map((admission) => { const p = adminAdmissionPresentation[admission.status]; const pendingThis = updateMutation.isPending && updateMutation.variables?.admissionId === admission.id; const name = `${admission.studentFirstName} ${admission.studentLastName}`; return <article key={admission.id} className="border border-slate-200 bg-white p-5 shadow-sm"><div className="flex flex-wrap items-start justify-between gap-3"><div><h2 className="font-semibold text-slate-950">{name}</h2><p className="mt-1 text-sm text-slate-600">Kimlik No: {admission.identityNumber}</p></div><AdminStatusBadge label={p.label} tone={p.tone} /></div><dl className="mt-4 grid gap-3 text-sm sm:grid-cols-2"><div><dt className="text-slate-500">Yurt dönemi</dt><dd className="mt-1 font-medium text-slate-900">{admission.dormitoryTermName}</dd></div><div><dt className="text-slate-500">Kabul tarihi</dt><dd className="mt-1 font-medium text-slate-900">{formatDate(admission.admissionDate)}</dd></div></dl>{admission.status === 'PENDING' && <div className="mt-5 flex flex-wrap gap-2 border-t border-slate-200 pt-4"><button type="button" onClick={() => decide(admission.id, 'APPROVED', name)} disabled={pendingThis} className="inline-flex min-h-9 items-center gap-2 bg-emerald-700 px-3 py-2 text-sm font-semibold text-white hover:bg-emerald-800 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-emerald-700 disabled:opacity-60">{pendingThis ? <LoaderCircle className="animate-spin" aria-hidden="true" size={16} /> : <Check aria-hidden="true" size={16} />} Onayla</button><button type="button" onClick={() => decide(admission.id, 'REJECTED', name)} disabled={pendingThis} className="inline-flex min-h-9 items-center gap-2 border border-red-700 px-3 py-2 text-sm font-semibold text-red-700 hover:bg-red-50 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-red-700 disabled:opacity-60"><X aria-hidden="true" size={16} /> Reddet</button></div>}</article>})}</div>}
    </div>}
  </section>
}
