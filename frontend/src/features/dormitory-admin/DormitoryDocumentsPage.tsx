import { Download, FileQuestion, FileText, LoaderCircle } from 'lucide-react'
import { useState } from 'react'
import { toast } from 'sonner'
import { PageHeader } from '../../components/common/PageHeader'
import type { StudentDocumentResponse } from '../../types/student'
import type { TermDocumentRequirementResponse } from '../../types/globalAdmin'
import { getApiErrorMessage } from '../../utils/apiError'
import { downloadBlob } from '../../utils/downloadBlob'
import { formatDateTime } from '../../utils/formatDateTime'
import { formatFileSize } from '../../utils/formatFileSize'
import { useAuth } from '../auth/useAuth'
import { AdminPageState } from './AdminPageState'
import { AdminStatusBadge } from './AdminStatusBadge'
import { adminAdmissionPresentation, adminDocumentPresentation } from './adminPresentation'
import { documentProcessPresentation, summarizeDocumentProcess } from './documentProcess'
import {
  useActiveTermDocumentRequirements,
  useAdmissionDocuments,
  useDormitoryAdmissions,
  useDownloadAdminDocument,
} from './dormitoryAdminQueries'

function UploadedDocumentCard({ document, requirementLabel, onDownload, downloading }: {
  document: StudentDocumentResponse
  requirementLabel: 'Zorunlu' | 'Opsiyonel' | 'Gereksinim dışı'
  onDownload: () => void
  downloading: boolean
}) {
  const presentation = adminDocumentPresentation[document.status]
  return <article className="border border-slate-200 bg-white p-5 shadow-sm"><div className="flex items-start gap-3">
    <span className="flex size-10 shrink-0 items-center justify-center bg-blue-50 text-blue-700"><FileText aria-hidden="true" size={20} /></span>
    <div className="min-w-0 flex-1"><div className="flex flex-wrap items-start justify-between gap-2"><div>
      <div className="flex flex-wrap items-center gap-2"><h3 className="font-semibold text-slate-950">{document.documentTypeName}</h3><span className="text-xs font-semibold text-slate-500">{requirementLabel}</span></div>
      <p className="mt-1 text-sm text-slate-600">{document.studentFirstName} {document.studentLastName}</p>
    </div><AdminStatusBadge label={presentation.label} tone={presentation.tone} /></div>
    <p className="mt-3 break-all text-sm font-medium text-slate-800">{document.originalFileName}</p>
    <div className="mt-2 flex flex-wrap gap-x-4 gap-y-1 text-xs text-slate-500"><span>{document.dormitoryTermName}</span><span>{document.contentType} · {formatFileSize(document.fileSize)}</span><time dateTime={document.uploadedAt}>{formatDateTime(document.uploadedAt)}</time></div>
    <button type="button" onClick={onDownload} disabled={downloading} className="mt-4 inline-flex min-h-9 items-center gap-2 border border-slate-300 px-3 py-2 text-sm font-semibold text-slate-700 hover:bg-slate-50 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-blue-700 disabled:opacity-60">{downloading ? <LoaderCircle className="animate-spin" aria-hidden="true" size={16} /> : <Download aria-hidden="true" size={16} />} İndir</button>
    </div></div></article>
}

function MissingDocumentCard({ requirement }: { requirement: TermDocumentRequirementResponse }) {
  return <article className="border border-amber-200 bg-amber-50/60 p-5 shadow-sm"><div className="flex items-start gap-3">
    <span className="flex size-10 shrink-0 items-center justify-center bg-amber-100 text-amber-800"><FileQuestion aria-hidden="true" size={20} /></span>
    <div className="min-w-0 flex-1"><div className="flex flex-wrap items-center justify-between gap-2"><div className="flex flex-wrap items-center gap-2"><h3 className="font-semibold text-slate-950">{requirement.documentTypeName}</h3><span className="text-xs font-semibold text-amber-800">{requirement.required ? 'Zorunlu' : 'Opsiyonel'}</span></div><AdminStatusBadge label="Eksik" tone={requirement.required ? 'warning' : 'neutral'} /></div>
    <p className="mt-3 text-sm font-medium text-slate-700">Henüz yüklenmedi</p>
    {requirement.documentTypeDescription && <p className="mt-2 text-xs leading-5 text-slate-600">{requirement.documentTypeDescription}</p>}
    </div></div></article>
}

export function DormitoryDocumentsPage() {
  const { user } = useAuth()
  const userId = user?.userId ?? 0
  const [admissionId, setAdmissionId] = useState<number | null>(null)
  const admissionsQuery = useDormitoryAdmissions(userId)
  const selectedAdmission = admissionsQuery.data?.find((admission) => admission.id === admissionId) ?? null
  const documentsQuery = useAdmissionDocuments(userId, admissionId)
  const requirementsQuery = useActiveTermDocumentRequirements(userId, selectedAdmission?.dormitoryTermId ?? null)
  const downloadMutation = useDownloadAdminDocument()
  const download = (id: number, fileName: string) => downloadMutation.mutate(id, { onSuccess: ({ blob }) => downloadBlob(blob, fileName), onError: (error: unknown) => toast.error(getApiErrorMessage(error, 'Belge indirilemedi.')) })
  const loadingSelection = admissionId !== null && (documentsQuery.isLoading || requirementsQuery.isLoading)
  const selectionError = documentsQuery.error ?? requirementsQuery.error

  return <section><PageHeader title="Belgeler" description="Aktif dönem kabul kayıtları üzerinden öğrencilerin belge durumlarını yönetsel amaçla izleyin." />
    <div className="mt-6 border border-blue-200 bg-blue-50 p-4 text-sm leading-6 text-blue-900">Belgeler, yetkili kabul kaydı seçilerek görüntülenir. Bu ekran değerlendirme kararı vermez.</div>
    {admissionsQuery.isLoading && <AdminPageState state="loading" title="Kabul kayıtları yükleniyor" message="Belge görüntülemek için öğrenci kabul kayıtları alınıyor..." />}
    {admissionsQuery.isError && <AdminPageState state="error" title="Kabul kayıtları alınamadı" message={getApiErrorMessage(admissionsQuery.error, 'Kabul kayıtları alınamadı.')} onRetry={() => void admissionsQuery.refetch()} />}
    {admissionsQuery.isSuccess && admissionsQuery.data.length === 0 && <AdminPageState state="empty" title="Kabul kaydı yok" message="Aktif dönemde belge görüntülenebilecek kabul kaydı bulunmuyor." />}
    {admissionsQuery.isSuccess && admissionsQuery.data.length > 0 && <div className="mt-5"><label htmlFor="admin-document-admission" className="block text-sm font-semibold text-slate-800">Öğrenci kabul kaydı</label><select id="admin-document-admission" value={admissionId ?? ''} onChange={(event) => setAdmissionId(event.target.value ? Number(event.target.value) : null)} className="mt-2 min-h-11 w-full max-w-xl border border-slate-300 bg-white px-3 py-2 text-sm focus:border-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-100"><option value="">Öğrenci seçin</option>{admissionsQuery.data.map((admission) => <option key={admission.id} value={admission.id}>{admission.studentFirstName} {admission.studentLastName} — {admission.identityNumber}</option>)}</select></div>}
    {admissionId === null && admissionsQuery.isSuccess && admissionsQuery.data.length > 0 && <div className="mt-5 border border-slate-200 bg-white p-6 text-center text-sm text-slate-600">Belgeleri görmek için bir öğrenci kabul kaydı seçin.</div>}
    {loadingSelection && <AdminPageState state="loading" title="Belge süreci yükleniyor" message="Belgeler ve dönem gereksinimleri alınıyor..." />}
    {selectionError && !loadingSelection && <AdminPageState state="error" title="Belge süreci yüklenemedi" message={getApiErrorMessage(selectionError, 'Belge süreci bilgileri alınamadı.')} onRetry={() => void Promise.all([documentsQuery.refetch(), requirementsQuery.refetch()])} />}
    {selectedAdmission && documentsQuery.isSuccess && requirementsQuery.isSuccess && (() => {
      const documents = documentsQuery.data
      const requirements = requirementsQuery.data
      const summary = summarizeDocumentProcess(requirements, documents)
      const process = documentProcessPresentation[summary.state]
      const admission = adminAdmissionPresentation[selectedAdmission.status]
      const documentsByType = new Map(documents.map((document) => [document.documentTypeId, document]))
      const renderRequirement = (requirement: TermDocumentRequirementResponse) => {
        const document = documentsByType.get(requirement.documentTypeId)
        if (!document) return <MissingDocumentCard key={requirement.id} requirement={requirement} />
        const pending = downloadMutation.isPending && downloadMutation.variables === document.id
        return <UploadedDocumentCard key={requirement.id} document={document} requirementLabel={requirement.required ? 'Zorunlu' : 'Opsiyonel'} downloading={pending} onDownload={() => download(document.id, document.originalFileName)} />
      }
      const required = requirements.filter((requirement) => requirement.required)
      const optional = requirements.filter((requirement) => !requirement.required)
      const requirementTypeIds = new Set(requirements.map((requirement) => requirement.documentTypeId))
      const otherUploadedDocuments = documents.filter((document) => !requirementTypeIds.has(document.documentTypeId))
      return <div className="mt-6 space-y-6">
        <div className="border border-slate-200 bg-white p-5 shadow-sm"><div className="flex flex-wrap items-center justify-between gap-4"><div><h2 className="text-lg font-semibold text-slate-950">{selectedAdmission.studentFirstName} {selectedAdmission.studentLastName}</h2><p className="mt-1 text-sm text-slate-600">{selectedAdmission.dormitoryTermName}</p></div><div className="flex flex-wrap gap-4"><div><p className="text-xs font-semibold uppercase tracking-wide text-slate-500">Kabul Durumu</p><div className="mt-2"><AdminStatusBadge label={admission.label} tone={admission.tone} /></div></div><div><p className="text-xs font-semibold uppercase tracking-wide text-slate-500">Belge Süreci</p><div className="mt-2"><AdminStatusBadge label={process.label} tone={process.tone} /></div></div></div></div>
          <dl className="mt-5 grid gap-3 sm:grid-cols-2 xl:grid-cols-5">{[['Tamamlanan', `${summary.approved} / ${summary.total}`], ['Eksik', summary.missing], ['Düzeltme gerekli', summary.revisionRequired], ['Reddedilen', summary.rejected], ['Değerlendirme bekleyen', summary.waitingReview]].map(([label, value]) => <div key={label} className="border border-slate-200 bg-slate-50 p-3"><dt className="text-xs font-medium text-slate-600">{label}</dt><dd className="mt-1 text-xl font-semibold text-slate-950">{value}</dd></div>)}</dl>
        </div>
        {requirements.length === 0 && <AdminPageState state="empty" title="Belge gereksinimi bulunmuyor" message="Seçilen kabul kaydının dönemi için aktif belge gereksinimi tanımlanmamış." />}
        {required.length > 0 && <section aria-labelledby="required-documents-title"><h2 id="required-documents-title" className="text-base font-semibold text-slate-950">Zorunlu belgeler</h2><div className="mt-3 grid gap-4 lg:grid-cols-2">{required.map(renderRequirement)}</div></section>}
        {optional.length > 0 && <section aria-labelledby="optional-documents-title"><div><h2 id="optional-documents-title" className="text-base font-semibold text-slate-950">Opsiyonel belgeler</h2><p className="mt-1 text-sm text-slate-600">Eksik opsiyonel belgeler tamamlanma durumunu etkilemez.</p></div><div className="mt-3 grid gap-4 lg:grid-cols-2">{optional.map(renderRequirement)}</div></section>}
        {otherUploadedDocuments.length > 0 && <section aria-labelledby="other-documents-title"><div><h2 id="other-documents-title" className="text-base font-semibold text-slate-950">Diğer yüklenmiş belgeler</h2><p className="mt-1 text-sm text-slate-600">Aktif dönem gereksinim setinde bulunmayan yüklenmiş belgeler.</p></div><div className="mt-3 grid gap-4 lg:grid-cols-2">{otherUploadedDocuments.map((document) => <UploadedDocumentCard key={document.id} document={document} requirementLabel="Gereksinim dışı" downloading={downloadMutation.isPending && downloadMutation.variables === document.id} onDownload={() => download(document.id, document.originalFileName)} />)}</div></section>}
      </div>
    })()}
  </section>
}
