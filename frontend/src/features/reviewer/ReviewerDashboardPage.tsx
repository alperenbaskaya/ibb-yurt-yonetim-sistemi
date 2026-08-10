import { AlertTriangle, CheckCircle2, Clock3, FileCheck2, Files, RefreshCw, UsersRound, XCircle } from 'lucide-react'
import { Link } from 'react-router-dom'
import { PageHeader } from '../../components/common/PageHeader'
import { getApiErrorMessage } from '../../utils/apiError'
import { formatDateTime } from '../../utils/formatDateTime'
import { useAuth } from '../auth/useAuth'
import { ReviewerPageState } from './ReviewerPageState'
import { ReviewRecordCard } from './ReviewRecordCard'
import { useReviewerDashboard } from './reviewerQueries'

interface StatCardProps {
  label: string
  value: number
  icon: typeof Files
  color: string
}

function StatCard({ label, value, icon: Icon, color }: StatCardProps) {
  return (
    <article className="border border-slate-200 bg-white p-4 shadow-sm">
      <div className="flex items-center justify-between gap-3">
        <div><p className="text-sm text-slate-600">{label}</p><p className="mt-1 text-2xl font-semibold text-slate-950">{value}</p></div>
        <span className={`flex size-10 items-center justify-center ${color}`}><Icon aria-hidden="true" size={20} /></span>
      </div>
    </article>
  )
}

export function ReviewerDashboardPage() {
  const { user } = useAuth()
  const dashboardQuery = useReviewerDashboard(user?.userId ?? 0)

  return (
    <section>
      <PageHeader title="Değerlendirici Kontrol Paneli" description="Yurdunuzdaki belge sürecini izleyin ve değerlendirme bekleyen işlere öncelik verin." />
      {dashboardQuery.isLoading && <ReviewerPageState state="loading" title="Kontrol paneli yükleniyor" message="Yurt belge sürecinin güncel durumu alınıyor..." />}
      {dashboardQuery.isError && <ReviewerPageState state="error" title="Kontrol paneli yüklenemedi" message={getApiErrorMessage(dashboardQuery.error, 'Değerlendirici kontrol paneli alınamadı.')} onRetry={() => void dashboardQuery.refetch()} />}

      {dashboardQuery.isSuccess && (() => {
        const dashboard = dashboardQuery.data
        return (
          <div className="mt-6 space-y-7">
            <article className="flex flex-col gap-4 border border-slate-200 bg-white p-5 shadow-sm sm:flex-row sm:items-center sm:justify-between sm:p-6">
              <div><p className="text-sm font-medium text-blue-800">Görev yaptığınız yurt</p><h2 className="mt-1 text-xl font-semibold text-slate-950">{dashboard.dormitoryName}</h2><p className="mt-2 text-sm font-semibold text-slate-700">Aktif dönem: {dashboard.activeTermName}</p><p className="mt-1 text-sm text-slate-600">{dashboard.firstName} {dashboard.lastName} · {dashboard.email}</p></div>
              <Link to="/reviewer/documents" className="inline-flex min-h-10 items-center justify-center gap-2 bg-blue-800 px-4 py-2 text-sm font-semibold text-white hover:bg-blue-900 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-blue-700"><Clock3 aria-hidden="true" size={17} /> Bekleyen belgeleri aç</Link>
            </article>

            <section aria-labelledby="review-status-title">
              <h2 id="review-status-title" className="text-lg font-semibold text-slate-950">Belge değerlendirme durumu</h2>
              <div className="mt-4 grid gap-3 sm:grid-cols-2 xl:grid-cols-4">
                <StatCard label="Değerlendirme bekleyen" value={dashboard.pendingDocumentCount} icon={Clock3} color="bg-blue-50 text-blue-700" />
                <StatCard label="Onaylanan" value={dashboard.approvedDocumentCount} icon={FileCheck2} color="bg-emerald-50 text-emerald-700" />
                <StatCard label="Düzeltme gereken" value={dashboard.revisionRequiredDocumentCount} icon={RefreshCw} color="bg-amber-50 text-amber-700" />
                <StatCard label="Reddedilen" value={dashboard.rejectedDocumentCount} icon={XCircle} color="bg-red-50 text-red-700" />
              </div>
            </section>

            <section aria-labelledby="student-progress-title" className="border border-slate-200 bg-white p-5 shadow-sm sm:p-6">
              <div className="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
                <div><h2 id="student-progress-title" className="text-lg font-semibold text-slate-950">Öğrenci belge süreci</h2><p className="mt-1 text-sm text-slate-600">Yurdun tamamına ait güncel ilerleme.</p></div>
                <div className="text-left sm:text-right"><p className="text-3xl font-semibold text-slate-950">%{dashboard.studentCompletionPercentage}</p><p className="text-xs font-medium text-slate-500">tamamlanma oranı</p></div>
              </div>
              <div className="mt-5 h-2.5 overflow-hidden bg-slate-200" role="progressbar" aria-label="Öğrenci belge süreci tamamlanma oranı" aria-valuemin={0} aria-valuemax={100} aria-valuenow={dashboard.studentCompletionPercentage}><div className="h-full bg-emerald-600" style={{ width: `${dashboard.studentCompletionPercentage}%` }} /></div>
              <div className="mt-5 grid gap-3 sm:grid-cols-2 xl:grid-cols-4">
                <StatCard label="Aktif öğrenci" value={dashboard.activeStudentCount} icon={UsersRound} color="bg-slate-100 text-slate-700" />
                <StatCard label="Süreci tamamlayan" value={dashboard.completedStudentCount} icon={CheckCircle2} color="bg-emerald-50 text-emerald-700" />
                <StatCard label="Süreci tamamlanmayan" value={dashboard.incompleteStudentCount} icon={Files} color="bg-blue-50 text-blue-700" />
                <StatCard label="İşlem gereken öğrenci" value={dashboard.actionRequiredStudentCount} icon={AlertTriangle} color="bg-amber-50 text-amber-700" />
              </div>
            </section>

            <div className="grid gap-6 xl:grid-cols-2">
              <section aria-labelledby="oldest-pending-title">
                <div className="flex items-end justify-between gap-3"><div><h2 id="oldest-pending-title" className="text-lg font-semibold text-slate-950">En uzun süredir bekleyenler</h2><p className="mt-1 text-sm text-slate-600">Backend tarafından en eski 10 belge.</p></div><Link to="/reviewer/documents" className="text-sm font-semibold text-blue-800 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-blue-700">Tümünü gör</Link></div>
                {dashboard.oldestPendingDocuments.length === 0 ? <div className="mt-4 border border-emerald-200 bg-emerald-50 p-5 text-sm text-emerald-800">Bekleyen belge bulunmuyor.</div> : <div className="mt-4 space-y-3">{dashboard.oldestPendingDocuments.map((document) => <article key={document.id} className="border border-slate-200 bg-white p-4 shadow-sm"><div className="flex flex-wrap items-start justify-between gap-2"><div><h3 className="font-semibold text-slate-900">{document.studentFirstName} {document.studentLastName}</h3><p className="mt-1 text-sm text-slate-600">{document.documentTypeName}</p></div><time className="text-xs font-medium text-slate-500" dateTime={document.uploadedAt}>{formatDateTime(document.uploadedAt)}</time></div></article>)}</div>}
              </section>

              <section aria-labelledby="pending-types-title">
                <h2 id="pending-types-title" className="text-lg font-semibold text-slate-950">Belge türüne göre bekleyenler</h2><p className="mt-1 text-sm text-slate-600">İş yükünün belge türlerine dağılımı.</p>
                {dashboard.pendingDocumentsByType.length === 0 ? <div className="mt-4 border border-emerald-200 bg-emerald-50 p-5 text-sm text-emerald-800">Bekleyen belge türü bulunmuyor.</div> : <div className="mt-4 divide-y divide-slate-200 border border-slate-200 bg-white shadow-sm">{dashboard.pendingDocumentsByType.map((item) => <div key={item.documentTypeId} className="flex items-center justify-between gap-3 px-4 py-3"><span className="text-sm font-medium text-slate-800">{item.documentTypeName}</span><span className="bg-blue-50 px-2.5 py-1 text-sm font-semibold text-blue-800">{item.pendingCount}</span></div>)}</div>}
              </section>
            </div>

            <section aria-labelledby="action-students-title">
              <h2 id="action-students-title" className="text-lg font-semibold text-slate-950">İşlem gereken öğrenciler</h2><p className="mt-1 text-sm text-slate-600">Backend tarafından önceliklendirilen ilk 10 öğrenci.</p>
              {dashboard.actionRequiredStudents.length === 0 ? <div className="mt-4 border border-emerald-200 bg-emerald-50 p-5 text-sm text-emerald-800">İşlem gereken öğrenci bulunmuyor.</div> : <div className="mt-4 grid gap-3 md:grid-cols-2">{dashboard.actionRequiredStudents.map((student) => <article key={student.admissionId} className="border border-slate-200 bg-white p-4 shadow-sm"><h3 className="font-semibold text-slate-900">{student.firstName} {student.lastName}</h3><div className="mt-3 flex flex-wrap gap-2 text-xs font-semibold"><span className="bg-amber-50 px-2 py-1 text-amber-800">Eksik: {student.missingDocumentCount}</span><span className="bg-orange-50 px-2 py-1 text-orange-800">Düzeltme: {student.revisionRequiredDocumentCount}</span><span className="bg-red-50 px-2 py-1 text-red-700">Ret: {student.rejectedDocumentCount}</span></div></article>)}</div>}
            </section>

            <section aria-labelledby="recent-reviews-title">
              <h2 id="recent-reviews-title" className="text-lg font-semibold text-slate-950">Yurttaki son değerlendirmeler</h2><p className="mt-1 text-sm text-slate-600">Tüm değerlendiricilerin aktif dönemdeki son 10 işlemi.</p>
              {dashboard.recentDormitoryReviews.length === 0 ? <div className="mt-4 border border-slate-200 bg-white p-5 text-sm text-slate-600">Henüz değerlendirme bulunmuyor.</div> : <div className="mt-4 grid gap-3 lg:grid-cols-2">{dashboard.recentDormitoryReviews.map((review) => <ReviewRecordCard key={review.id} review={review} showReviewer />)}</div>}
            </section>
          </div>
        )
      })()}
    </section>
  )
}
