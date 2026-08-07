import {
  AlertTriangle,
  CalendarDays,
  CheckCircle2,
  Clock3,
  FileCheck2,
  FileText,
  MessageSquareText,
} from 'lucide-react'
import { Link } from 'react-router-dom'
import { PageHeader } from '../../components/common/PageHeader'
import { getApiErrorMessage } from '../../utils/apiError'
import { formatDate } from '../../utils/formatDate'
import { formatDateTime } from '../../utils/formatDateTime'
import { useAuth } from '../auth/useAuth'
import { StudentPageState } from './StudentPageState'
import { StudentStatusBadge } from './StudentStatusBadge'
import {
  actionReasonPresentation,
  admissionStatusPresentation,
  documentStatusPresentation,
  reviewDecisionPresentation,
  uploadPeriodPresentation,
} from './studentPresentation'
import { useStudentDashboard } from './studentQueries'

export function StudentDashboardPage() {
  const { user } = useAuth()
  const dashboardQuery = useStudentDashboard(user?.userId ?? 0)

  return (
    <section>
      <PageHeader
        title="Öğrenci Kontrol Paneli"
        description="Belge sürecinizin güncel durumunu ve sizden beklenen işlemleri takip edin."
      />

      {dashboardQuery.isLoading && (
        <StudentPageState state="loading" title="Kontrol paneli yükleniyor" message="Güncel kabul ve belge bilgileriniz alınıyor..." />
      )}

      {dashboardQuery.isError && (
        <StudentPageState
          state="error"
          title="Kontrol paneli yüklenemedi"
          message={getApiErrorMessage(dashboardQuery.error, 'Öğrenci bilgileriniz alınırken bir sorun oluştu.')}
          onRetry={() => void dashboardQuery.refetch()}
        />
      )}

      {dashboardQuery.isSuccess && (() => {
        const dashboard = dashboardQuery.data
        const admission = admissionStatusPresentation[dashboard.admissionStatus]
        const uploadPeriod = uploadPeriodPresentation[dashboard.uploadPeriodStatus]

        return (
          <div className="mt-6 space-y-6">
            <div className="grid gap-4 xl:grid-cols-[1.5fr_1fr]">
              <article className="border border-slate-200 bg-white p-5 shadow-sm sm:p-6">
                <div className="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
                  <div>
                    <p className="text-sm font-medium text-blue-800">Mevcut kabul durumu</p>
                    <h2 className="mt-1 text-xl font-semibold text-slate-950">{dashboard.dormitoryName}</h2>
                    <p className="mt-1 text-sm text-slate-600">{dashboard.dormitoryTermName}</p>
                  </div>
                  <StudentStatusBadge label={admission.label} tone={admission.tone} />
                </div>
                <p className="mt-5 border-l-4 border-blue-700 bg-blue-50 px-4 py-3 text-sm leading-6 text-slate-700">
                  {dashboard.admissionStatusMessage}
                </p>
              </article>

              <article className="border border-slate-200 bg-white p-5 shadow-sm sm:p-6">
                <div className="flex items-center justify-between gap-3">
                  <div>
                    <p className="text-sm font-medium text-slate-600">Belge tamamlanma oranı</p>
                    <p className="mt-1 text-3xl font-semibold text-slate-950">%{dashboard.completionPercentage}</p>
                  </div>
                  <span className="flex size-11 items-center justify-center bg-emerald-50 text-emerald-700">
                    <FileCheck2 aria-hidden="true" size={23} />
                  </span>
                </div>
                <div
                  className="mt-5 h-2.5 overflow-hidden bg-slate-200"
                  role="progressbar"
                  aria-label="Zorunlu belge tamamlanma oranı"
                  aria-valuemin={0}
                  aria-valuemax={100}
                  aria-valuenow={dashboard.completionPercentage}
                >
                  <div className="h-full bg-emerald-600" style={{ width: `${dashboard.completionPercentage}%` }} />
                </div>
                <p className="mt-3 text-sm text-slate-600">
                  {dashboard.approvedRequiredDocuments} / {dashboard.totalRequiredDocuments} zorunlu belge onaylandı
                </p>
                {dashboard.documentProcessCompleted && (
                  <p className="mt-3 inline-flex items-center gap-2 text-sm font-semibold text-emerald-700">
                    <CheckCircle2 aria-hidden="true" size={17} /> Belge süreciniz tamamlandı.
                  </p>
                )}
              </article>
            </div>

            <article className="border border-slate-200 bg-white p-5 shadow-sm sm:p-6">
              <div className="flex flex-wrap items-center justify-between gap-3">
                <div className="flex items-center gap-3">
                  <span className="flex size-10 items-center justify-center bg-blue-50 text-blue-700">
                    <CalendarDays aria-hidden="true" size={21} />
                  </span>
                  <div>
                    <h2 className="font-semibold text-slate-950">Belge yükleme dönemi</h2>
                    <p className="text-sm text-slate-600">{formatDate(dashboard.documentUploadStartDate)} – {formatDate(dashboard.documentUploadEndDate)}</p>
                  </div>
                </div>
                <StudentStatusBadge label={uploadPeriod.label} tone={uploadPeriod.tone} />
              </div>
              <div className="mt-4 flex items-center gap-2 bg-slate-50 px-4 py-3 text-sm text-slate-700">
                <Clock3 aria-hidden="true" size={17} className="shrink-0 text-slate-500" />
                {dashboard.uploadPeriodStatus === 'OPEN'
                  ? `Yükleme döneminin bitmesine ${dashboard.remainingUploadDays} gün kaldı.`
                  : dashboard.uploadPeriodStatus === 'NOT_STARTED'
                    ? 'Belge yükleme dönemi henüz başlamadı.'
                    : 'Belge yükleme dönemi sona erdi.'}
              </div>
            </article>

            <section aria-labelledby="action-required-title">
              <div className="flex flex-wrap items-end justify-between gap-3">
                <div>
                  <h2 id="action-required-title" className="text-lg font-semibold text-slate-950">Sizden beklenenler</h2>
                  <p className="mt-1 text-sm text-slate-600">İşlem gerektiren zorunlu belgeleriniz.</p>
                </div>
                <Link to="/student/documents" className="text-sm font-semibold text-blue-800 hover:text-blue-950 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-blue-700">
                  Belgelerime git
                </Link>
              </div>
              {dashboard.actionRequiredDocuments.length === 0 ? (
                <div className="mt-4 flex items-center gap-3 border border-emerald-200 bg-emerald-50 p-4 text-sm text-emerald-800">
                  <CheckCircle2 aria-hidden="true" size={20} /> Şu anda işlem yapmanızı gerektiren zorunlu bir belge yok.
                </div>
              ) : (
                <div className="mt-4 grid gap-3 md:grid-cols-2">
                  {dashboard.actionRequiredDocuments.map((action) => {
                    const presentation = actionReasonPresentation[action.reason]
                    return (
                      <article key={action.documentTypeId} className="border border-slate-200 bg-white p-4 shadow-sm">
                        <div className="flex items-start gap-3">
                          <AlertTriangle aria-hidden="true" className={presentation.tone === 'error' ? 'text-red-700' : 'text-amber-700'} size={20} />
                          <div>
                            <h3 className="font-semibold text-slate-900">{action.documentTypeName}</h3>
                            <div className="mt-2"><StudentStatusBadge label={presentation.label} tone={presentation.tone} /></div>
                            <p className="mt-3 text-sm leading-6 text-slate-600">
                              {action.reason === 'REJECTED'
                                ? 'Bu belge reddedilmiştir ve yeniden yüklenemez.'
                                : action.message}
                            </p>
                          </div>
                        </div>
                      </article>
                    )
                  })}
                </div>
              )}
            </section>

            <section aria-labelledby="required-documents-title">
              <h2 id="required-documents-title" className="text-lg font-semibold text-slate-950">Belge özeti</h2>
              {dashboard.documents.length === 0 ? (
                <div className="mt-4 border border-slate-200 bg-white p-6 text-center text-sm text-slate-600">Bu dönem için tanımlanmış belge gereksinimi bulunmuyor.</div>
              ) : (
                <div className="mt-4 grid gap-3 lg:grid-cols-2">
                  {dashboard.documents.map((document) => {
                    const status = document.status ? documentStatusPresentation[document.status] : null
                    return (
                      <article key={document.requirementId} className="flex items-start gap-3 border border-slate-200 bg-white p-4 shadow-sm">
                        <FileText aria-hidden="true" className="mt-0.5 shrink-0 text-blue-700" size={20} />
                        <div className="min-w-0 flex-1">
                          <div className="flex flex-wrap items-start justify-between gap-2">
                            <h3 className="font-semibold text-slate-900">{document.documentTypeName}</h3>
                            {status ? <StudentStatusBadge label={status.label} tone={status.tone} /> : <StudentStatusBadge label="Yüklenmedi" tone="neutral" />}
                          </div>
                          {document.documentTypeDescription && <p className="mt-2 text-sm leading-6 text-slate-600">{document.documentTypeDescription}</p>}
                          <p className="mt-2 text-xs font-medium text-slate-500">{document.required ? 'Zorunlu belge' : 'İsteğe bağlı belge'}</p>
                        </div>
                      </article>
                    )
                  })}
                </div>
              )}
            </section>

            <section aria-labelledby="last-review-title">
              <h2 id="last-review-title" className="text-lg font-semibold text-slate-950">Son değerlendirme</h2>
              {dashboard.lastReview ? (() => {
                const review = dashboard.lastReview
                const decision = reviewDecisionPresentation[review.decision]
                return (
                  <article className={`mt-4 border p-5 shadow-sm ${decision.tone === 'error' ? 'border-red-200 bg-red-50' : decision.tone === 'warning' ? 'border-amber-200 bg-amber-50' : 'border-emerald-200 bg-emerald-50'}`}>
                    <div className="flex items-start gap-3">
                      <MessageSquareText aria-hidden="true" className="mt-0.5 shrink-0 text-slate-700" size={21} />
                      <div>
                        <div className="flex flex-wrap items-center gap-2">
                          <h3 className="font-semibold text-slate-950">{review.documentTypeName}</h3>
                          <StudentStatusBadge label={decision.label} tone={decision.tone} />
                        </div>
                        {review.comment && <p className="mt-3 whitespace-pre-wrap text-sm leading-6 text-slate-700">{review.comment}</p>}
                        <p className="mt-3 text-xs font-medium text-slate-500">{formatDateTime(review.reviewedAt)}</p>
                      </div>
                    </div>
                  </article>
                )
              })() : (
                <div className="mt-4 border border-slate-200 bg-white p-5 text-sm text-slate-600">Henüz bir belge değerlendirmesi bulunmuyor.</div>
              )}
            </section>
          </div>
        )
      })()}
    </section>
  )
}
