import type { DocumentReviewDecision, StudentDocumentStatus } from '../../types/student'

export type ReviewerTone = 'success' | 'info' | 'warning' | 'error' | 'neutral'

export const reviewerDocumentStatusPresentation: Record<
  StudentDocumentStatus,
  { label: string; tone: ReviewerTone }
> = {
  UPLOADED: { label: 'Değerlendirme Bekliyor', tone: 'info' },
  APPROVED: { label: 'Onaylandı', tone: 'success' },
  REJECTED: { label: 'Reddedildi', tone: 'error' },
  REVISION_REQUIRED: { label: 'Düzeltme Gerekli', tone: 'warning' },
}

export const reviewerDecisionPresentation: Record<
  DocumentReviewDecision,
  { label: string; tone: ReviewerTone }
> = {
  APPROVED: { label: 'Onaylandı', tone: 'success' },
  REJECTED: { label: 'Reddedildi', tone: 'error' },
  REVISION_REQUIRED: { label: 'Düzeltme İstendi', tone: 'warning' },
}

export const reviewerToneClasses: Record<ReviewerTone, string> = {
  success: 'border-emerald-200 bg-emerald-50 text-emerald-700',
  info: 'border-blue-200 bg-blue-50 text-blue-700',
  warning: 'border-amber-200 bg-amber-50 text-amber-800',
  error: 'border-red-200 bg-red-50 text-red-700',
  neutral: 'border-slate-200 bg-slate-100 text-slate-700',
}
