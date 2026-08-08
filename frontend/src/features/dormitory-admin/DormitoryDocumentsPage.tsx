import { Download, FileText, LoaderCircle } from 'lucide-react'
import { useState } from 'react'
import { toast } from 'sonner'
import { PageHeader } from '../../components/common/PageHeader'
import { getApiErrorMessage } from '../../utils/apiError'
import { downloadBlob } from '../../utils/downloadBlob'
import { formatDateTime } from '../../utils/formatDateTime'
import { formatFileSize } from '../../utils/formatFileSize'
import { useAuth } from '../auth/useAuth'
import { AdminPageState } from './AdminPageState'
import { AdminStatusBadge } from './AdminStatusBadge'
import { adminDocumentPresentation } from './adminPresentation'
import { useAdmissionDocuments, useDormitoryAdmissions, useDownloadAdminDocument } from './dormitoryAdminQueries'

export function DormitoryDocumentsPage() {
  const { user } = useAuth()
  const userId = user?.userId ?? 0
  const [admissionId, setAdmissionId] = useState<number | null>(null)
  const admissionsQuery = useDormitoryAdmissions(userId)
  const documentsQuery = useAdmissionDocuments(userId, admissionId)
  const downloadMutation = useDownloadAdminDocument()
  const download = (id: number, fileName: string) => downloadMutation.mutate(id, { onSuccess: ({ blob }) => downloadBlob(blob, fileName), onError: (error: unknown) => toast.error(getApiErrorMessage(error, 'Belge indirilemedi.')) })

  return <section><PageHeader title="Belgeler" description="Aktif dönem kabul kayıtları üzerinden öğrencilerin belge durumlarını yönetsel amaçla izleyin." />
    <div className="mt-6 border border-blue-200 bg-blue-50 p-4 text-sm leading-6 text-blue-900">Backend yurt-geneli tek bir belge listesi sunmadığı için belgeler, yetkili kabul kaydı seçilerek görüntülenir. Bu ekran değerlendirme kararı vermez.</div>
    {admissionsQuery.isLoading && <AdminPageState state="loading" title="Kabul kayıtları yükleniyor" message="Belge görüntülemek için öğrenci kabul kayıtları alınıyor..." />}
    {admissionsQuery.isError && <AdminPageState state="error" title="Kabul kayıtları alınamadı" message={getApiErrorMessage(admissionsQuery.error, 'Kabul kayıtları alınamadı.')} onRetry={() => void admissionsQuery.refetch()} />}
    {admissionsQuery.isSuccess && admissionsQuery.data.length === 0 && <AdminPageState state="empty" title="Kabul kaydı yok" message="Aktif dönemde belge görüntülenebilecek kabul kaydı bulunmuyor." />}
    {admissionsQuery.isSuccess && admissionsQuery.data.length > 0 && <div className="mt-5"><label htmlFor="admin-document-admission" className="block text-sm font-semibold text-slate-800">Öğrenci kabul kaydı</label><select id="admin-document-admission" value={admissionId ?? ''} onChange={(event) => setAdmissionId(event.target.value ? Number(event.target.value) : null)} className="mt-2 min-h-11 w-full max-w-xl border border-slate-300 bg-white px-3 py-2 text-sm focus:border-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-100"><option value="">Öğrenci seçin</option>{admissionsQuery.data.map((admission) => <option key={admission.id} value={admission.id}>{admission.studentFirstName} {admission.studentLastName} — {admission.identityNumber}</option>)}</select></div>}
    {admissionId === null && admissionsQuery.isSuccess && admissionsQuery.data.length > 0 && <div className="mt-5 border border-slate-200 bg-white p-6 text-center text-sm text-slate-600">Belgeleri görmek için bir öğrenci kabul kaydı seçin.</div>}
    {documentsQuery.isLoading && <AdminPageState state="loading" title="Belgeler yükleniyor" message="Seçilen kabul kaydına ait belgeler alınıyor..." />}
    {documentsQuery.isError && <AdminPageState state="error" title="Belgeler yüklenemedi" message={getApiErrorMessage(documentsQuery.error, 'Öğrenci belgeleri alınamadı.')} onRetry={() => void documentsQuery.refetch()} />}
    {documentsQuery.isSuccess && documentsQuery.data.length === 0 && <AdminPageState state="empty" title="Yüklenmiş belge yok" message="Seçilen kabul kaydına ait yüklenmiş belge bulunmuyor." />}
    {documentsQuery.isSuccess && documentsQuery.data.length > 0 && <div className="mt-6 grid gap-4 lg:grid-cols-2">{documentsQuery.data.map((item) => { const p = adminDocumentPresentation[item.status]; const pending = downloadMutation.isPending && downloadMutation.variables === item.id; return <article key={item.id} className="border border-slate-200 bg-white p-5 shadow-sm"><div className="flex items-start gap-3"><span className="flex size-10 shrink-0 items-center justify-center bg-blue-50 text-blue-700"><FileText aria-hidden="true" size={20} /></span><div className="min-w-0 flex-1"><div className="flex flex-wrap items-start justify-between gap-2"><div><h2 className="font-semibold text-slate-950">{item.documentTypeName}</h2><p className="mt-1 text-sm text-slate-600">{item.studentFirstName} {item.studentLastName}</p></div><AdminStatusBadge label={p.label} tone={p.tone} /></div><p className="mt-3 break-all text-sm font-medium text-slate-800">{item.originalFileName}</p><div className="mt-2 flex flex-wrap gap-x-4 gap-y-1 text-xs text-slate-500"><span>{item.dormitoryTermName}</span><span>{item.contentType} · {formatFileSize(item.fileSize)}</span><time dateTime={item.uploadedAt}>{formatDateTime(item.uploadedAt)}</time></div><button type="button" onClick={() => download(item.id, item.originalFileName)} disabled={pending} className="mt-4 inline-flex min-h-9 items-center gap-2 border border-slate-300 px-3 py-2 text-sm font-semibold text-slate-700 hover:bg-slate-50 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-blue-700 disabled:opacity-60">{pending ? <LoaderCircle className="animate-spin" aria-hidden="true" size={16} /> : <Download aria-hidden="true" size={16} />} İndir</button></div></div></article>})}</div>}
  </section>
}
