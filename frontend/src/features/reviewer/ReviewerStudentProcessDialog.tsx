import { CheckCircle2, FileQuestion, FileText, LoaderCircle, X } from 'lucide-react'
import { useEffect, useRef } from 'react'
import { useNavigate } from 'react-router-dom'
import type { ReviewerStudentResponse } from '../../types/reviewer'
import { getApiErrorMessage } from '../../utils/apiError'
import { ReviewerStatusBadge } from './ReviewerStatusBadge'
import { reviewerDocumentStatusPresentation } from './reviewerPresentation'
import { useReviewerStudentDocumentProcess } from './reviewerQueries'

interface ReviewerStudentProcessDialogProps {
  userId: number
  student: ReviewerStudentResponse
  onClose: () => void
}

export function ReviewerStudentProcessDialog({ userId, student, onClose }: ReviewerStudentProcessDialogProps) {
  const dialogRef = useRef<HTMLDialogElement>(null)
  const navigate = useNavigate()
  const query = useReviewerStudentDocumentProcess(userId, student.studentId)

  useEffect(() => {
    const dialog = dialogRef.current
    if (dialog && !dialog.open) dialog.showModal()
  }, [])

  const processPresentation = query.data?.completed
    ? { label: 'Tamamlandı', tone: 'success' as const }
    : query.data && query.data.rejectedDocumentCount > 0
      ? { label: 'Reddedilen Belge Var', tone: 'error' as const }
      : query.data && query.data.revisionRequiredDocumentCount > 0
        ? { label: 'Düzeltme Gerekli', tone: 'warning' as const }
        : query.data && query.data.uploadedRequiredDocumentCount > 0
          ? { label: 'Değerlendirme Bekliyor', tone: 'info' as const }
          : { label: 'Bekliyor', tone: 'warning' as const }

  const openPendingDocument = (documentTypeId: number) => {
    onClose()
    void navigate('/reviewer/documents', {
      state: { reviewTarget: { studentId: student.studentId, documentTypeId } },
    })
  }

  return (
    <dialog
      ref={dialogRef}
      aria-labelledby="reviewer-student-process-title"
      aria-describedby="reviewer-student-process-description"
      onCancel={(event) => { event.preventDefault(); onClose() }}
      onClose={onClose}
      className="m-auto max-h-[90vh] w-[min(44rem,calc(100%-2rem))] overflow-hidden border border-slate-200 bg-white p-0 text-slate-900 shadow-2xl backdrop:bg-slate-950/50"
    >
      <div className="flex max-h-[90vh] flex-col">
        <header className="flex items-start justify-between gap-4 border-b border-slate-200 px-5 py-4 sm:px-6">
          <div className="min-w-0"><h2 id="reviewer-student-process-title" className="text-lg font-semibold text-slate-950">{student.firstName} {student.lastName}</h2><p id="reviewer-student-process-description" className="mt-1 text-sm text-slate-600">Kimlik numarası: {student.identityNumber}</p></div>
          <button type="button" onClick={onClose} aria-label="Belge süreci penceresini kapat" className="flex size-10 shrink-0 items-center justify-center border border-slate-300 text-slate-600 hover:bg-slate-50 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-blue-700"><X aria-hidden="true" size={20} /></button>
        </header>

        <div className="overflow-y-auto px-5 py-5 sm:px-6">
          {query.isLoading && <div className="flex min-h-48 flex-col items-center justify-center text-center"><LoaderCircle className="animate-spin text-blue-700" aria-hidden="true" size={28} /><p className="mt-3 font-semibold text-slate-900">Belge süreci yükleniyor</p><p className="mt-1 text-sm text-slate-600">Zorunlu belge durumları alınıyor...</p></div>}
          {query.isError && <div className="border border-red-200 bg-red-50 p-4"><p className="font-semibold text-red-800">Belge süreci yüklenemedi</p><p className="mt-1 text-sm text-red-700">{getApiErrorMessage(query.error, 'Belge süreci bilgileri alınamadı.')}</p><button type="button" onClick={() => void query.refetch()} className="mt-3 min-h-9 border border-red-300 bg-white px-3 py-2 text-sm font-semibold text-red-800 hover:bg-red-100 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-red-700">Tekrar dene</button></div>}
          {query.isSuccess && (() => {
            const process = query.data
            return <div className="space-y-5">
              <section className={`border p-4 ${process.completed ? 'border-emerald-200 bg-emerald-50' : 'border-slate-200 bg-slate-50'}`}><div className="flex flex-wrap items-start justify-between gap-3"><div><p className="text-xs font-semibold uppercase tracking-wide text-slate-500">Belge Süreci</p><p className="mt-2 text-lg font-semibold text-slate-950">{process.approvedRequiredDocumentCount} / {process.totalRequiredDocumentCount} belge onaylandı</p><p className="mt-1 text-sm text-slate-600">{process.dormitoryTermName}</p></div><ReviewerStatusBadge label={processPresentation.label} tone={processPresentation.tone} /></div>{process.completed && <p className="mt-4 flex items-start gap-2 text-sm font-semibold text-emerald-800"><CheckCircle2 className="mt-0.5 shrink-0" aria-hidden="true" size={18} />Kesin kayıt için gerekli tüm belgeler tamamlandı ve onaylandı.</p>}</section>

              <dl className="grid gap-2 sm:grid-cols-2 lg:grid-cols-4">{[['Eksik', process.missingRequiredDocumentCount], ['Bekleyen', process.uploadedRequiredDocumentCount], ['Düzeltme', process.revisionRequiredDocumentCount], ['Reddedilen', process.rejectedDocumentCount]].map(([label, value]) => <div key={label} className="border border-slate-200 p-3"><dt className="text-xs text-slate-500">{label}</dt><dd className="mt-1 text-xl font-semibold text-slate-950">{value}</dd></div>)}</dl>

              {process.missingRequiredDocumentCount === 0 && !process.completed && <p className="border border-blue-200 bg-blue-50 p-3 text-sm text-blue-800">Eksik belge yok.</p>}
              {process.requiredDocuments.length === 0 ? <div className="border border-slate-200 bg-slate-50 p-4 text-sm text-slate-600">Aktif dönem için zorunlu belge tanımlanmamış.</div> : <section aria-labelledby="required-process-documents"><h3 id="required-process-documents" className="font-semibold text-slate-950">Zorunlu belgeler</h3><ul className="mt-3 divide-y divide-slate-200 border border-slate-200">{process.requiredDocuments.map((item) => {
                const presentation = item.status ? reviewerDocumentStatusPresentation[item.status] : { label: 'Henüz yüklenmedi', tone: 'warning' as const }
                const isPendingReview = item.status === 'UPLOADED'
                const content = <><span className="flex min-w-0 items-center gap-3">{item.uploaded ? <FileText className="shrink-0 text-blue-700" aria-hidden="true" size={18} /> : <FileQuestion className="shrink-0 text-amber-700" aria-hidden="true" size={18} />}<span className="font-medium text-slate-900">{item.documentTypeName}</span></span><ReviewerStatusBadge label={presentation.label} tone={presentation.tone} /></>
                return <li key={item.documentTypeId}>{isPendingReview ? <button type="button" onClick={() => openPendingDocument(item.documentTypeId)} className="flex w-full items-center justify-between gap-3 p-4 text-left hover:bg-blue-50 focus-visible:outline-2 focus-visible:outline-offset-[-2px] focus-visible:outline-blue-700" aria-label={`${item.documentTypeName} belgesini değerlendir`}>{content}</button> : <div className="flex items-center justify-between gap-3 p-4">{content}</div>}</li>
              })}</ul></section>}
            </div>
          })()}
        </div>
      </div>
    </dialog>
  )
}
