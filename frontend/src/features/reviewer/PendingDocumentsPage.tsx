import { Download, Eye, FileText, LoaderCircle, X } from 'lucide-react'
import { useState } from 'react'
import { toast } from 'sonner'
import { PageHeader } from '../../components/common/PageHeader'
import type { CreateDocumentReviewRequest } from '../../types/reviewer'
import { getApiErrorMessage } from '../../utils/apiError'
import { formatDateTime } from '../../utils/formatDateTime'
import { formatFileSize } from '../../utils/formatFileSize'
import { useAuth } from '../auth/useAuth'
import { DocumentReviewForm } from './DocumentReviewForm'
import { ReviewerPageState } from './ReviewerPageState'
import { ReviewerStatusBadge } from './ReviewerStatusBadge'
import { ReviewRecordCard } from './ReviewRecordCard'
import { reviewerDocumentStatusPresentation } from './reviewerPresentation'
import {
  useCreateDocumentReview,
  useDocumentReviewHistory,
  useDownloadReviewerDocument,
  usePendingReviewerDocuments,
  useReviewerDocument,
} from './reviewerQueries'

export function PendingDocumentsPage() {
  const { user } = useAuth()
  const userId = user?.userId ?? 0
  const [selectedDocumentId, setSelectedDocumentId] = useState<number | null>(null)
  const [documentTypeFilter, setDocumentTypeFilter] = useState('ALL')
  const pendingQuery = usePendingReviewerDocuments(userId)
  const detailQuery = useReviewerDocument(userId, selectedDocumentId)
  const historyQuery = useDocumentReviewHistory(userId, selectedDocumentId)
  const reviewMutation = useCreateDocumentReview(userId)
  const downloadMutation = useDownloadReviewerDocument()

  const documents = pendingQuery.data ?? []
  const documentTypes = Array.from(
    new Map(documents.map((document) => [document.documentTypeId, document.documentTypeName])).entries(),
  )
  const visibleDocuments = documentTypeFilter === 'ALL'
    ? documents
    : documents.filter((document) => String(document.documentTypeId) === documentTypeFilter)

  const handleReview = async (request: CreateDocumentReviewRequest) => {
    await reviewMutation.mutateAsync(request)
    toast.success('Belge değerlendirmesi kaydedildi.')
  }

  const handleDownload = (documentId: number, fileName: string) => {
    downloadMutation.mutate(documentId, {
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
      onError: (error: unknown) => toast.error(
        getApiErrorMessage(error, 'Belge indirilemedi. Lütfen tekrar deneyin.'),
      ),
    })
  }

  return (
    <section>
      <PageHeader title="Bekleyen Belgeler" description="Yurdunuzun aktif döneminde değerlendirme bekleyen belgeleri inceleyin ve karara bağlayın." />
      {pendingQuery.isLoading && <ReviewerPageState state="loading" title="Bekleyen belgeler yükleniyor" message="Değerlendirme kuyruğunuz alınıyor..." />}
      {pendingQuery.isError && <ReviewerPageState state="error" title="Bekleyen belgeler yüklenemedi" message={getApiErrorMessage(pendingQuery.error, 'Değerlendirme bekleyen belgeler alınamadı.')} onRetry={() => void pendingQuery.refetch()} />}
      {pendingQuery.isSuccess && documents.length === 0 && <ReviewerPageState state="empty" title="Bekleyen belge yok" message="Yurdunuzda şu anda değerlendirme bekleyen bir belge bulunmuyor." />}

      {pendingQuery.isSuccess && documents.length > 0 && (
        <div className="mt-6 space-y-6">
          <div className="flex flex-col gap-3 border border-slate-200 bg-white p-4 shadow-sm sm:flex-row sm:items-end sm:justify-between">
            <div><p className="text-sm font-semibold text-slate-900">{documents.length} belge değerlendirme bekliyor</p><p className="mt-1 text-xs text-slate-500">Belgeler yüklenme tarihine göre backend sırasıyla gösterilir.</p></div>
            <div className="w-full sm:w-72"><label htmlFor="document-type-filter" className="block text-xs font-semibold text-slate-700">Belge türü</label><select id="document-type-filter" value={documentTypeFilter} onChange={(event) => setDocumentTypeFilter(event.target.value)} className="mt-1 min-h-10 w-full border border-slate-300 bg-white px-3 py-2 text-sm focus:border-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-100"><option value="ALL">Tüm belge türleri</option>{documentTypes.map(([id, name]) => <option key={id} value={id}>{name}</option>)}</select></div>
          </div>

          {visibleDocuments.length === 0 ? (
            <div className="border border-slate-200 bg-white p-6 text-center text-sm text-slate-600">Seçilen belge türünde bekleyen kayıt bulunmuyor.</div>
          ) : (
            <div className="grid gap-3 lg:grid-cols-2">
              {visibleDocuments.map((document) => (
                <article key={document.id} className="border border-slate-200 bg-white p-4 shadow-sm">
                  <div className="flex items-start gap-3">
                    <span className="flex size-10 shrink-0 items-center justify-center bg-blue-50 text-blue-700"><FileText aria-hidden="true" size={20} /></span>
                    <div className="min-w-0 flex-1">
                      <div className="flex flex-wrap items-start justify-between gap-2"><div><h2 className="font-semibold text-slate-950">{document.studentFirstName} {document.studentLastName}</h2><p className="mt-1 text-sm text-slate-600">{document.documentTypeName}</p></div><ReviewerStatusBadge label="Değerlendirme Bekliyor" tone="info" /></div>
                      <p className="mt-3 truncate text-sm font-medium text-slate-700">{document.originalFileName}</p>
                      <div className="mt-2 flex flex-wrap gap-x-4 gap-y-1 text-xs text-slate-500"><span>{document.dormitoryTermName}</span><time dateTime={document.uploadedAt}>{formatDateTime(document.uploadedAt)}</time></div>
                      <button type="button" onClick={() => setSelectedDocumentId(document.id)} className="mt-4 inline-flex min-h-9 items-center justify-center gap-2 border border-blue-700 px-3 py-2 text-sm font-semibold text-blue-800 hover:bg-blue-50 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-blue-700"><Eye aria-hidden="true" size={16} /> İncele ve değerlendir</button>
                    </div>
                  </div>
                </article>
              ))}
            </div>
          )}
        </div>
      )}

      {selectedDocumentId !== null && (
        <section className="mt-8 border-t-2 border-blue-800 pt-6" aria-labelledby="document-detail-title">
          <div className="flex items-center justify-between gap-3"><h2 id="document-detail-title" className="text-xl font-semibold text-slate-950">Belge ayrıntısı</h2><button type="button" onClick={() => setSelectedDocumentId(null)} className="flex size-9 items-center justify-center text-slate-600 hover:bg-slate-100 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-blue-700" aria-label="Belge ayrıntısını kapat"><X aria-hidden="true" size={20} /></button></div>
          {detailQuery.isLoading && <ReviewerPageState state="loading" title="Belge yükleniyor" message="Belge ayrıntıları alınıyor..." />}
          {detailQuery.isError && <ReviewerPageState state="error" title="Belge alınamadı" message={getApiErrorMessage(detailQuery.error, 'Belge ayrıntısı alınamadı.')} onRetry={() => void detailQuery.refetch()} />}
          {detailQuery.isSuccess && (() => {
            const document = detailQuery.data
            const status = reviewerDocumentStatusPresentation[document.status]
            return (
              <div className="mt-4 grid gap-6 xl:grid-cols-[1.05fr_0.95fr]">
                <article className="border border-slate-200 bg-white p-5 shadow-sm">
                  <div className="flex flex-wrap items-start justify-between gap-3"><div><h3 className="text-lg font-semibold text-slate-950">{document.studentFirstName} {document.studentLastName}</h3><p className="mt-1 text-sm text-slate-600">{document.documentTypeName}</p></div><ReviewerStatusBadge label={status.label} tone={status.tone} /></div>
                  <dl className="mt-5 grid gap-4 text-sm sm:grid-cols-2"><div><dt className="font-medium text-slate-500">Dosya</dt><dd className="mt-1 break-all font-medium text-slate-900">{document.originalFileName}</dd></div><div><dt className="font-medium text-slate-500">Dosya türü / boyutu</dt><dd className="mt-1 text-slate-900">{document.contentType} · {formatFileSize(document.fileSize)}</dd></div><div><dt className="font-medium text-slate-500">Yurt dönemi</dt><dd className="mt-1 text-slate-900">{document.dormitoryTermName}</dd></div><div><dt className="font-medium text-slate-500">Yüklenme</dt><dd className="mt-1 text-slate-900">{formatDateTime(document.uploadedAt)}</dd></div></dl>
                  <button type="button" onClick={() => handleDownload(document.id, document.originalFileName)} disabled={downloadMutation.isPending} className="mt-5 inline-flex min-h-10 items-center justify-center gap-2 border border-slate-300 px-4 py-2 text-sm font-semibold text-slate-700 hover:bg-slate-50 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-blue-700 disabled:opacity-60">{downloadMutation.isPending ? <LoaderCircle className="animate-spin" aria-hidden="true" size={17} /> : <Download aria-hidden="true" size={17} />} Belgeyi indir</button>
                  {document.status === 'UPLOADED' ? <DocumentReviewForm studentDocumentId={document.id} onSubmitReview={handleReview} /> : <p className="mt-5 border border-amber-200 bg-amber-50 p-4 text-sm text-amber-900">Bu belge artık değerlendirme beklemiyor. Yeni bir karar gönderilemez.</p>}
                </article>

                <section aria-labelledby="document-history-title"><h3 id="document-history-title" className="font-semibold text-slate-950">Değerlendirme geçmişi</h3>{historyQuery.isLoading && <p className="mt-4 text-sm text-slate-600" role="status">Geçmiş yükleniyor...</p>}{historyQuery.isError && <div className="mt-4 border border-red-200 bg-red-50 p-4 text-sm text-red-800" role="alert">{getApiErrorMessage(historyQuery.error, 'Değerlendirme geçmişi alınamadı.')} <button type="button" onClick={() => void historyQuery.refetch()} className="ml-2 font-semibold underline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-red-700">Tekrar dene</button></div>}{historyQuery.isSuccess && historyQuery.data.length === 0 && <div className="mt-4 border border-slate-200 bg-white p-4 text-sm text-slate-600">Bu belge için önceki değerlendirme bulunmuyor.</div>}{historyQuery.isSuccess && historyQuery.data.length > 0 && <div className="mt-4 space-y-3">{historyQuery.data.map((review) => <ReviewRecordCard key={review.id} review={review} showReviewer />)}</div>}</section>
              </div>
            )
          })()}
        </section>
      )}
    </section>
  )
}
