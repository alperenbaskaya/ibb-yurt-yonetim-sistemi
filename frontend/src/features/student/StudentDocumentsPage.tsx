import { Download, FileText, LoaderCircle, LockKeyhole, MessageSquareText } from 'lucide-react'
import { toast } from 'sonner'
import { PageHeader } from '../../components/common/PageHeader'
import type {
  AdmissionStatus,
  DocumentReviewDecision,
  StudentDocumentRequirementStatusResponse,
  StudentDocumentUploadRequest,
  UploadPeriodStatus,
} from '../../types/student'
import { getApiErrorMessage } from '../../utils/apiError'
import { formatDateTime } from '../../utils/formatDateTime'
import { useAuth } from '../auth/useAuth'
import { DocumentUploadControl } from './DocumentUploadControl'
import { StudentPageState } from './StudentPageState'
import { StudentStatusBadge } from './StudentStatusBadge'
import { documentStatusPresentation, reviewDecisionPresentation } from './studentPresentation'
import { useDownloadStudentDocument, useStudentDashboard, useStudentDocumentRequirements, useUploadStudentDocument } from './studentQueries'

function getUploadRestrictionMessage(
  admissionStatus: AdmissionStatus,
  uploadPeriodStatus: UploadPeriodStatus,
): string | null {
  if (admissionStatus !== 'APPROVED') return 'Belge yükleyebilmek için yurt kabulünüzün onaylanmış olması gerekir.'
  if (uploadPeriodStatus === 'NOT_STARTED') return 'Belge yükleme dönemi henüz başlamadı.'
  if (uploadPeriodStatus === 'CLOSED') return 'Belge yükleme dönemi sona erdi.'
  return null
}

interface RequirementCardProps {
  requirement: StudentDocumentRequirementStatusResponse
  canUploadGlobally: boolean
  latestReviewComment: string | null
  latestReviewDecision: DocumentReviewDecision | null
  latestReviewDate: string | null
  uploadIsPending: boolean
  downloadIsPending: boolean
  onUpload: (request: StudentDocumentUploadRequest) => Promise<void>
  onDownload: (studentDocumentId: number, fileName: string) => void
}

function RequirementCard({ requirement, canUploadGlobally, latestReviewComment, latestReviewDecision, latestReviewDate, uploadIsPending, downloadIsPending, onUpload, onDownload }: RequirementCardProps) {
  const status = requirement.status ? documentStatusPresentation[requirement.status] : null
  const allowsFirstUpload = !requirement.uploaded && requirement.studentDocumentId === null
  const allowsReupload = requirement.status === 'REVISION_REQUIRED'
  const showUpload = canUploadGlobally && (allowsFirstUpload || allowsReupload)

  return (
    <article className={`border bg-white p-5 shadow-sm ${requirement.status === 'REVISION_REQUIRED' ? 'border-amber-300' : requirement.status === 'REJECTED' ? 'border-red-300' : 'border-slate-200'}`}>
      <div className="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
        <div className="flex min-w-0 items-start gap-3">
          <span className="flex size-10 shrink-0 items-center justify-center bg-blue-50 text-blue-700"><FileText aria-hidden="true" size={21} /></span>
          <div className="min-w-0">
            <h2 className="font-semibold text-slate-950">{requirement.documentTypeName}</h2>
            {requirement.documentTypeDescription && <p className="mt-1 text-sm leading-6 text-slate-600">{requirement.documentTypeDescription}</p>}
            <p className="mt-2 text-xs font-semibold text-slate-500">{requirement.required ? 'Zorunlu belge' : 'İsteğe bağlı belge'}</p>
          </div>
        </div>
        {status ? <StudentStatusBadge label={status.label} tone={status.tone} /> : <StudentStatusBadge label="Yüklenmedi" tone="neutral" />}
      </div>

      {requirement.uploaded && (
        <div className="mt-4 grid gap-3 border-t border-slate-200 pt-4 sm:grid-cols-[1fr_auto] sm:items-center">
          <div className="min-w-0">
            <p className="truncate text-sm font-medium text-slate-800">{requirement.originalFileName}</p>
            {requirement.uploadedAt && <p className="mt-1 text-xs text-slate-500">Yüklenme: {formatDateTime(requirement.uploadedAt)}</p>}
          </div>
          {requirement.studentDocumentId !== null && requirement.originalFileName && (
            <button type="button" onClick={() => onDownload(requirement.studentDocumentId!, requirement.originalFileName!)} disabled={downloadIsPending} className="inline-flex min-h-9 items-center justify-center gap-2 border border-slate-300 px-3 py-2 text-sm font-semibold text-slate-700 hover:bg-slate-50 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-blue-700 disabled:cursor-not-allowed disabled:opacity-60">
              {downloadIsPending ? <LoaderCircle className="animate-spin" aria-hidden="true" size={16} /> : <Download aria-hidden="true" size={16} />} İndir
            </button>
          )}
        </div>
      )}

      {(requirement.status === 'REVISION_REQUIRED' || requirement.status === 'REJECTED') && (
        <div className={`mt-4 border p-4 ${requirement.status === 'REVISION_REQUIRED' ? 'border-amber-200 bg-amber-50' : 'border-red-200 bg-red-50'}`}>
          <div className="flex items-start gap-2">
            <MessageSquareText aria-hidden="true" className="mt-0.5 shrink-0" size={18} />
            <div>
              <p className="text-sm font-semibold text-slate-900">{requirement.status === 'REVISION_REQUIRED' ? 'Düzeltme açıklaması' : 'Ret durumu'}</p>
              {latestReviewComment ? <p className="mt-2 whitespace-pre-wrap text-sm leading-6 text-slate-700">{latestReviewComment}</p> : <p className="mt-2 text-sm leading-6 text-slate-700">Bu belgeye ait son değerlendirme açıklaması sunulmadı.</p>}
              {latestReviewDecision && latestReviewDate && <p className="mt-2 text-xs font-medium text-slate-500">{reviewDecisionPresentation[latestReviewDecision].label} · {formatDateTime(latestReviewDate)}</p>}
              {requirement.status === 'REJECTED' && <p className="mt-3 text-sm font-semibold text-red-800">Reddedilmiş belge yeniden yüklenemez.</p>}
            </div>
          </div>
        </div>
      )}

      {showUpload && <DocumentUploadControl documentTypeId={requirement.documentTypeId} documentTypeName={requirement.documentTypeName} actionLabel={allowsReupload ? 'Düzeltilmiş belgeyi yükle' : 'Belgeyi yükle'} isPending={uploadIsPending} onUpload={onUpload} />}
      {!showUpload && !requirement.uploaded && !canUploadGlobally && <p className="mt-4 inline-flex items-center gap-2 border-t border-slate-200 pt-4 text-sm text-slate-600"><LockKeyhole aria-hidden="true" size={16} /> Belge yükleme şu anda kullanılamıyor.</p>}
    </article>
  )
}

export function StudentDocumentsPage() {
  const { user } = useAuth()
  const userId = user?.userId ?? 0
  const dashboardQuery = useStudentDashboard(userId)
  const requirementsQuery = useStudentDocumentRequirements(userId)
  const uploadMutation = useUploadStudentDocument(userId)
  const downloadMutation = useDownloadStudentDocument()
  const isLoading = dashboardQuery.isLoading || requirementsQuery.isLoading
  const error = dashboardQuery.error ?? requirementsQuery.error

  const handleUpload = async (request: StudentDocumentUploadRequest) => {
    await uploadMutation.mutateAsync(request)
    toast.success('Belgeniz başarıyla yüklendi.')
  }

  const handleDownload = (studentDocumentId: number, fileName: string) => {
    downloadMutation.mutate(studentDocumentId, {
      onSuccess: ({ blob }) => {
        const objectUrl = URL.createObjectURL(blob)
        const anchor = document.createElement('a')
        anchor.href = objectUrl
        anchor.download = fileName
        document.body.appendChild(anchor)
        anchor.click()
        anchor.remove()
        URL.revokeObjectURL(objectUrl)
      },
      onError: (downloadError: unknown) => toast.error(getApiErrorMessage(downloadError, 'Belge indirilemedi. Lütfen tekrar deneyin.')),
    })
  }

  return (
    <section>
      <PageHeader title="Belgelerim" description="Aktif yurt döneminiz için tanımlanan belgeleri görüntüleyin ve uygun durumlarda yükleyin." />
      {isLoading && <StudentPageState state="loading" title="Belgeler yükleniyor" message="Belge gereksinimleriniz ve yükleme durumunuz alınıyor..." />}
      {error && !isLoading && <StudentPageState state="error" title="Belgeler yüklenemedi" message={getApiErrorMessage(error, 'Belge bilgileriniz alınırken bir sorun oluştu.')} onRetry={() => void Promise.all([dashboardQuery.refetch(), requirementsQuery.refetch()])} />}

      {dashboardQuery.isSuccess && requirementsQuery.isSuccess && (() => {
        const dashboard = dashboardQuery.data
        const requirements = requirementsQuery.data
        const canUploadGlobally = dashboard.admissionStatus === 'APPROVED' && dashboard.uploadPeriodStatus === 'OPEN'
        const restrictionMessage = getUploadRestrictionMessage(dashboard.admissionStatus, dashboard.uploadPeriodStatus)
        if (requirements.length === 0) return <StudentPageState state="empty" title="Belge gereksinimi bulunmuyor" message="Aktif yurt döneminiz için henüz bir belge gereksinimi tanımlanmamış." />

        return (
          <div className="mt-6 space-y-5">
            <div className="flex flex-col gap-3 border border-slate-200 bg-white p-4 shadow-sm sm:flex-row sm:items-center sm:justify-between">
              <div><p className="font-semibold text-slate-900">{dashboard.dormitoryName}</p><p className="mt-1 text-sm text-slate-600">{dashboard.dormitoryTermName}</p></div>
              <p className="text-sm font-medium text-slate-600">{requirements.filter((requirement) => requirement.uploaded).length} / {requirements.length} belge yüklendi</p>
            </div>
            {restrictionMessage && <div className="flex items-start gap-3 border border-amber-200 bg-amber-50 p-4 text-sm leading-6 text-amber-900" role="status"><LockKeyhole aria-hidden="true" className="mt-0.5 shrink-0" size={18} />{restrictionMessage}</div>}
            <div className="grid gap-4 xl:grid-cols-2">
              {requirements.map((requirement) => {
                const latestReview = dashboard.lastReview?.studentDocumentId === requirement.studentDocumentId ? dashboard.lastReview : null
                return <RequirementCard key={requirement.requirementId} requirement={requirement} canUploadGlobally={canUploadGlobally} latestReviewComment={latestReview?.comment ?? null} latestReviewDecision={latestReview?.decision ?? null} latestReviewDate={latestReview?.reviewedAt ?? null} uploadIsPending={uploadMutation.isPending && uploadMutation.variables?.documentTypeId === requirement.documentTypeId} downloadIsPending={downloadMutation.isPending && downloadMutation.variables === requirement.studentDocumentId} onUpload={handleUpload} onDownload={handleDownload} />
              })}
            </div>
          </div>
        )
      })()}
    </section>
  )
}
