import { CheckCircle2, Download, FileQuestion, FileText, LoaderCircle, X } from 'lucide-react'
import { useEffect, useRef } from 'react'
import { toast } from 'sonner'
import type { AdminStudentDocumentInspectionItemResponse } from '../../types/adminStudentInspection'
import type { StudentDocumentStatus } from '../../types/student'
import { getApiErrorMessage } from '../../utils/apiError'
import { downloadBlob } from '../../utils/downloadBlob'
import { formatDateTime } from '../../utils/formatDateTime'
import { AdminStatusBadge } from '../dormitory-admin/AdminStatusBadge'
import type { AdminTone } from '../dormitory-admin/adminPresentation'
import { useAdminStudentInspection, useDownloadInspectedStudentDocument } from './adminStudentInspectionQueries'

const documentPresentation: Record<StudentDocumentStatus, { label: string; tone: AdminTone }> = {
  UPLOADED: { label: 'Değerlendirme Bekliyor', tone: 'info' },
  APPROVED: { label: 'Onaylandı', tone: 'success' },
  REVISION_REQUIRED: { label: 'Düzeltme Gerekli', tone: 'warning' },
  REJECTED: { label: 'Reddedildi', tone: 'error' },
}

const decisionLabels = {
  APPROVED: 'Onaylandı',
  REVISION_REQUIRED: 'Düzeltme istendi',
  REJECTED: 'Reddedildi',
} as const

interface Props {
  userId: number
  student: { id: number; firstName: string; lastName: string; identityNumber: string }
  onClose: () => void
}

function InspectionDocument({ item, onDownload, downloading }: {
  item: AdminStudentDocumentInspectionItemResponse
  onDownload: () => void
  downloading: boolean
}) {
  const presentation = item.status
    ? documentPresentation[item.status]
    : { label: 'Henüz yüklenmedi', tone: 'warning' as const }

  return <article className="border border-slate-200 bg-white p-4">
    <div className="flex items-start justify-between gap-3"><div className="flex min-w-0 items-start gap-3">{item.uploaded ? <FileText className="mt-0.5 shrink-0 text-blue-700" aria-hidden="true" size={19} /> : <FileQuestion className="mt-0.5 shrink-0 text-amber-700" aria-hidden="true" size={19} />}<div><h4 className="font-semibold text-slate-950">{item.documentTypeName}</h4><p className="mt-1 text-xs font-semibold text-slate-500">Zorunlu</p></div></div><AdminStatusBadge label={presentation.label} tone={presentation.tone} /></div>
    {!item.uploaded ? <p className="mt-4 text-sm text-slate-600">Henüz yüklenmedi</p> : <div className="mt-4 space-y-3">
      <div><p className="break-all text-sm font-medium text-slate-800">{item.originalFileName}</p>{item.uploadedAt && <p className="mt-1 text-xs text-slate-500">Yüklendi: {formatDateTime(item.uploadedAt)}</p>}</div>
      {item.latestReview ? <dl className="grid gap-2 border border-slate-200 bg-slate-50 p-3 text-sm sm:grid-cols-2"><div><dt className="text-xs text-slate-500">Son değerlendiren</dt><dd className="mt-1 font-medium">{item.latestReview.reviewerFirstName} {item.latestReview.reviewerLastName}</dd></div><div><dt className="text-xs text-slate-500">Değerlendirme tarihi</dt><dd className="mt-1 font-medium">{formatDateTime(item.latestReview.reviewedAt)}</dd></div><div><dt className="text-xs text-slate-500">Karar</dt><dd className="mt-1 font-medium">{decisionLabels[item.latestReview.decision]}</dd></div>{item.latestReview.comment && <div className="sm:col-span-2"><dt className="text-xs text-slate-500">Yorum</dt><dd className="mt-1 whitespace-pre-wrap text-slate-700">{item.latestReview.comment}</dd></div>}</dl> : <p className="text-sm text-slate-600">Henüz değerlendirilmedi.</p>}
      <div className="flex flex-wrap items-center gap-3"><button type="button" onClick={onDownload} disabled={downloading} className="inline-flex min-h-9 items-center gap-2 border border-slate-300 px-3 py-2 text-sm font-semibold text-slate-700 hover:bg-slate-50 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-blue-700 disabled:opacity-60">{downloading ? <LoaderCircle className="animate-spin" aria-hidden="true" size={16} /> : <Download aria-hidden="true" size={16} />} İndir</button>
        {item.reviewHistory.length > 0 && <details className="w-full"><summary className="cursor-pointer text-sm font-semibold text-blue-700 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-blue-700">Değerlendirme Geçmişi ({item.reviewHistory.length})</summary><ol className="mt-3 space-y-2 border-l-2 border-slate-200 pl-4">{item.reviewHistory.map((review, index) => <li key={`${review.reviewedAt}-${index}`} className="text-sm"><p className="text-xs text-slate-500">{formatDateTime(review.reviewedAt)}</p><p className="mt-1 font-medium text-slate-900">{review.reviewerFirstName} {review.reviewerLastName} · {decisionLabels[review.decision]}</p>{review.comment && <p className="mt-1 whitespace-pre-wrap text-slate-600">“{review.comment}”</p>}</li>)}</ol></details>}
      </div>
    </div>}
  </article>
}

export function AdminStudentInspectionDialog({ userId, student, onClose }: Props) {
  const dialogRef = useRef<HTMLDialogElement>(null)
  const query = useAdminStudentInspection(userId, student.id)
  const downloadMutation = useDownloadInspectedStudentDocument()

  useEffect(() => { if (dialogRef.current && !dialogRef.current.open) dialogRef.current.showModal() }, [])

  const download = (item: AdminStudentDocumentInspectionItemResponse) => {
    if (item.studentDocumentId === null || item.originalFileName === null) return
    downloadMutation.mutate(item.studentDocumentId, {
      onSuccess: (blob) => downloadBlob(blob, item.originalFileName!),
      onError: (error: unknown) => toast.error(getApiErrorMessage(error, 'Belge indirilemedi.')),
    })
  }

  const process = query.data
  const processPresentation = process?.completed
    ? { label: 'Tamamlandı', tone: 'success' as const }
    : process && process.rejectedDocumentCount > 0
      ? { label: 'Reddedilen Belge Var', tone: 'error' as const }
      : process && process.revisionRequiredDocumentCount > 0
        ? { label: 'Düzeltme Gerekli', tone: 'warning' as const }
        : process && process.uploadedRequiredDocumentCount > 0
          ? { label: 'Değerlendirme Bekliyor', tone: 'info' as const }
          : { label: 'Bekliyor', tone: 'warning' as const }

  return <dialog ref={dialogRef} aria-labelledby="admin-student-inspection-title" aria-describedby="admin-student-inspection-description" onCancel={(event) => { event.preventDefault(); onClose() }} onClose={onClose} className="m-auto max-h-[92vh] w-[min(58rem,calc(100%-2rem))] overflow-hidden border border-slate-200 bg-white p-0 text-slate-900 shadow-2xl backdrop:bg-slate-950/50">
    <div className="flex max-h-[92vh] flex-col"><header className="flex items-start justify-between gap-4 border-b border-slate-200 px-5 py-4 sm:px-6"><div><h2 id="admin-student-inspection-title" className="text-lg font-semibold text-slate-950">{student.firstName} {student.lastName}</h2><p id="admin-student-inspection-description" className="mt-1 text-sm text-slate-600">Belge süreci incelemesi · Kimlik: {student.identityNumber}</p></div><button type="button" onClick={onClose} aria-label="Belge inceleme penceresini kapat" className="flex size-10 shrink-0 items-center justify-center border border-slate-300 text-slate-600 hover:bg-slate-50 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-blue-700"><X aria-hidden="true" size={20} /></button></header>
      <div className="overflow-y-auto px-5 py-5 sm:px-6">
        {query.isLoading && <div className="flex min-h-52 flex-col items-center justify-center"><LoaderCircle className="animate-spin text-blue-700" aria-hidden="true" size={28} /><p className="mt-3 font-semibold">Belge incelemesi yükleniyor</p></div>}
        {query.isError && <div className="border border-red-200 bg-red-50 p-4"><p className="font-semibold text-red-800">Belge incelemesi yüklenemedi</p><p className="mt-1 text-sm text-red-700">{getApiErrorMessage(query.error, 'Belge süreci alınamadı.')}</p><button type="button" onClick={() => void query.refetch()} className="mt-3 min-h-9 border border-red-300 bg-white px-3 py-2 text-sm font-semibold text-red-800">Tekrar dene</button></div>}
        {process && <div className="space-y-5"><section className={`border p-4 ${process.completed ? 'border-emerald-200 bg-emerald-50' : 'border-slate-200 bg-slate-50'}`}><div className="flex flex-wrap items-start justify-between gap-3"><div><p className="font-semibold text-slate-950">{process.dormitoryName}</p><p className="mt-1 text-sm text-slate-600">{process.dormitoryTermName}</p><p className="mt-3 text-lg font-semibold">{process.approvedRequiredDocumentCount} / {process.totalRequiredDocumentCount} belge onaylandı</p></div><AdminStatusBadge label={processPresentation.label} tone={processPresentation.tone} /></div>{process.completed && <p className="mt-4 flex items-start gap-2 text-sm font-semibold text-emerald-800"><CheckCircle2 className="mt-0.5 shrink-0" aria-hidden="true" size={18} />Kesin kayıt için gerekli tüm belgeler tamamlandı ve onaylandı.</p>}</section>
          <dl className="grid gap-2 sm:grid-cols-2 lg:grid-cols-4">{[['Eksik', process.missingRequiredDocumentCount], ['Bekleyen', process.uploadedRequiredDocumentCount], ['Düzeltme', process.revisionRequiredDocumentCount], ['Reddedilen', process.rejectedDocumentCount]].map(([label, value]) => <div key={label} className="border border-slate-200 p-3"><dt className="text-xs text-slate-500">{label}</dt><dd className="mt-1 text-xl font-semibold">{value}</dd></div>)}</dl>
          {process.requiredDocuments.length === 0 ? <div className="border border-slate-200 bg-slate-50 p-4 text-sm text-slate-600">Aktif dönem için zorunlu belge tanımlanmamış.</div> : <section aria-labelledby="admin-inspection-documents"><h3 id="admin-inspection-documents" className="font-semibold text-slate-950">Zorunlu belgeler</h3><div className="mt-3 grid gap-3">{process.requiredDocuments.map((item) => <InspectionDocument key={item.documentTypeId} item={item} onDownload={() => download(item)} downloading={downloadMutation.isPending && downloadMutation.variables === item.studentDocumentId} />)}</div></section>}
        </div>}
      </div>
    </div>
  </dialog>
}
